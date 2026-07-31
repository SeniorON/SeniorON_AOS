package com.example.senior_on.ui.child.notification.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationMessageUiState
import com.example.senior_on.ui.child.notification.NotificationScreenUiState
import com.example.senior_on.ui.child.notification.apiType
import com.example.senior_on.ui.child.notification.emptyNotificationScreenUiState
import com.example.senior_on.ui.child.notification.parentNotConnectedNotificationScreenUiState
import com.example.senior_on.ui.child.notification.toUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val home: NotificationScreenUiState = emptyNotificationScreenUiState(),
    val histories: Map<NotificationCategory, List<NotificationMessageUiState>> =
        emptyMap(),
    val inactivityThresholdHours: Int = DefaultInactivityThresholdHours,
    val isInactivitySettingLoading: Boolean = false,
    val isInactivitySettingSaving: Boolean = false,
    val detailMessages: Map<Long, NotificationMessageUiState> = emptyMap(),
    val isDetailLoading: Boolean = false,
    val errorMessage: String? = null,
)

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val familyRepository: FamilyServerRepository?,
    private val homeRepository: HomeServerRepository?,
    private val eventRepository: EventRepository?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private var inactivityTargetUserId: Long? = null
    private val confirmedSettings = mutableMapOf<NotificationCategory, Boolean>()
    private val desiredSettings = mutableMapOf<NotificationCategory, Boolean>()
    private val settingSyncJobs = mutableMapOf<NotificationCategory, Job>()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val familyMembers = familyRepository?.getMembers()
                val parentUserId = familyMembers
                    ?.firstOrNull { member ->
                        member.role.equals(ParentRole, ignoreCase = true)
                    }
                    ?.id
                    ?.takeIf { it > 0L }
                if (familyMembers != null && parentUserId == null) {
                    return@runCatching NotificationHomeLoadResult(
                        home = parentNotConnectedNotificationScreenUiState(),
                    )
                }
                inactivityTargetUserId = parentUserId

                val home = async { repository.getHome() }
                val parentOnline = async { repository.isParentDeviceOnline() }
                val inactivitySetting = parentUserId?.let { targetUserId ->
                    async {
                        runCatching {
                            repository.getInactivitySetting(targetUserId)
                        }.getOrNull()
                    }
                }
                val hasHomeAddress = async {
                    homeRepository
                        ?.getHome()
                        ?.seniorAddress
                        ?.isNotBlank()
                        ?: true
                }
                NotificationHomeLoadResult(
                    home = home.await().toUiState(
                        isParentDeviceOnline = parentOnline.await(),
                        hasHomeAddress = hasHomeAddress.await(),
                    ),
                    inactivityThresholdHours = inactivitySetting
                        ?.await()
                        ?.thresholdHours,
                )
            }.onSuccess { result ->
                val mergedHome = result.home.copy(
                    sections = result.home.sections.map { section ->
                        val category = section.category
                        if (category != NotificationCategory.Sos) {
                            if (settingSyncJobs[category]?.isActive != true) {
                                confirmedSettings[category] = section.enabled
                            }
                            section.copy(
                                enabled = desiredSettings[category] ?: section.enabled,
                                detectionStandardTime = if (
                                    category == NotificationCategory.Inactivity
                                ) {
                                    result.inactivityThresholdHours
                                        ?.let(::formatThresholdHours)
                                        ?: section.detectionStandardTime
                                } else {
                                    section.detectionStandardTime
                                },
                            )
                        } else {
                            section
                        }
                    },
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        home = mergedHome,
                        inactivityThresholdHours =
                            result.inactivityThresholdHours
                                ?: it.inactivityThresholdHours,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    fun loadHistory(category: NotificationCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.getNotifications(category.apiType).items
                    .map { notification -> notification.toUiState(category) }
            }.onSuccess { messages ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        histories = it.histories + (category to messages),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    fun updateSetting(
        category: NotificationCategory,
        enabled: Boolean,
    ) {
        if (category == NotificationCategory.Sos) return
        val currentEnabled = _uiState.value.home.sections
            .firstOrNull { it.category == category }
            ?.enabled
            ?: return

        confirmedSettings.putIfAbsent(category, currentEnabled)
        desiredSettings[category] = enabled
        setLocalSetting(category, enabled)

        if (settingSyncJobs[category]?.isActive != true) {
            settingSyncJobs[category] = viewModelScope.launch {
                synchronizeSetting(category)
            }
        }
    }

    fun openNotification(
        category: NotificationCategory,
        message: NotificationMessageUiState,
    ) {
        message.notificationId?.let { notificationId ->
            markNotificationRead(notificationId)
        }
        val eventId = message.eventId ?: return
        val events = eventRepository ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isDetailLoading = true, errorMessage = null)
            }
            runCatching {
                events.getDetail(eventId).toUiState(category, message)
            }.onSuccess { detail ->
                Log.d(
                    DetailLogTag,
                    "Loaded eventId=$eventId, battery=${detail.deviceBattery}, " +
                        "hasCoordinates=${detail.latitude != null && detail.longitude != null}, " +
                        "occurredAtMillis=${detail.occurredAtMillis}",
                )
                _uiState.update {
                    it.copy(
                        detailMessages = it.detailMessages + (eventId to detail),
                        isDetailLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    fun loadInactivitySetting() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInactivitySettingLoading = true,
                    errorMessage = null,
                )
            }
            runCatching {
                val targetUserId = resolveInactivityTargetUserId()
                repository.getInactivitySetting(targetUserId)
            }.onSuccess { setting ->
                _uiState.update {
                    it.copy(
                        home = it.home.withInactivityThresholdHours(
                            setting.thresholdHours,
                        ),
                        inactivityThresholdHours = setting.thresholdHours,
                        isInactivitySettingLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isInactivitySettingLoading = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    fun updateInactivitySetting(
        thresholdHours: Int,
        onSuccess: () -> Unit,
    ) {
        if (_uiState.value.isInactivitySettingSaving) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isInactivitySettingSaving = true,
                    errorMessage = null,
                )
            }
            runCatching {
                val targetUserId = resolveInactivityTargetUserId()
                repository.updateInactivitySetting(
                    userId = targetUserId,
                    thresholdHours = thresholdHours,
                )
            }.onSuccess { setting ->
                _uiState.update {
                    it.copy(
                        home = it.home.withInactivityThresholdHours(
                            setting.thresholdHours,
                        ),
                        inactivityThresholdHours = setting.thresholdHours,
                        isInactivitySettingSaving = false,
                    )
                }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isInactivitySettingSaving = false,
                        errorMessage = throwable.message,
                    )
                }
            }
        }
    }

    private suspend fun resolveInactivityTargetUserId(): Long {
        inactivityTargetUserId?.let { return it }
        val targetUserId = requireNotNull(familyRepository) {
            "가족 구성원 Repository가 필요합니다."
        }.getMembers()
            .firstOrNull { member ->
                member.role.equals(ParentRole, ignoreCase = true)
            }
            ?.id
            ?.takeIf { it > 0L }
            ?: error("가족 구성원 중 시니어 사용자를 찾을 수 없습니다.")
        inactivityTargetUserId = targetUserId
        return targetUserId
    }

    private suspend fun synchronizeSetting(category: NotificationCategory) {
        while (true) {
            val target = desiredSettings[category] ?: break
            val result = runCatching {
                repository.updateSetting(category.apiType, target)
            }

            if (result.isSuccess) {
                confirmedSettings[category] = target
                if (desiredSettings[category] == target) {
                    desiredSettings.remove(category)
                    break
                }
                continue
            }

            if (desiredSettings[category] == target) {
                desiredSettings.remove(category)
                confirmedSettings[category]?.let { confirmed ->
                    setLocalSetting(category, confirmed)
                }
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message)
                }
                break
            }
        }
        settingSyncJobs.remove(category)
    }

    private fun setLocalSetting(
        category: NotificationCategory,
        enabled: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(
                home = state.home.copy(
                    sections = state.home.sections.map { section ->
                        if (section.category == category) {
                            section.copy(enabled = enabled)
                        } else {
                            section
                        }
                    },
                ),
                errorMessage = null,
            )
        }
    }

    private fun markNotificationRead(notificationId: Long) {
        _uiState.update { state ->
            state.copy(
                home = state.home.copy(
                    sections = state.home.sections.map { section ->
                        section.copy(
                            messages = section.messages.map { message ->
                                if (message.notificationId == notificationId) {
                                    message.copy(isRead = true)
                                } else {
                                    message
                                }
                            },
                        )
                    },
                ),
                histories = state.histories.mapValues { (_, messages) ->
                    messages.map { message ->
                        if (message.notificationId == notificationId) {
                            message.copy(isRead = true)
                        } else {
                            message
                        }
                    }
                },
            )
        }
        viewModelScope.launch {
            runCatching { repository.markRead(notificationId) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message)
                    }
                }
        }
    }

    class Factory(
        private val repository: NotificationRepository,
        private val familyRepository: FamilyServerRepository?,
        private val homeRepository: HomeServerRepository?,
        private val eventRepository: EventRepository?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NotificationViewModel::class.java))
            return NotificationViewModel(
                repository = repository,
                familyRepository = familyRepository,
                homeRepository = homeRepository,
                eventRepository = eventRepository,
            ) as T
        }
    }

    private companion object {
        const val ParentRole = "PARENT"
        const val DetailLogTag = "SeniorOnNotificationDetail"
    }
}

private data class NotificationHomeLoadResult(
    val home: NotificationScreenUiState,
    val inactivityThresholdHours: Int? = null,
)

private fun NotificationScreenUiState.withInactivityThresholdHours(
    thresholdHours: Int,
): NotificationScreenUiState = copy(
    sections = sections.map { section ->
        if (section.category == NotificationCategory.Inactivity) {
            section.copy(
                detectionStandardTime = formatThresholdHours(thresholdHours),
            )
        } else {
            section
        }
    },
)

private fun formatThresholdHours(thresholdHours: Int): String =
    "${thresholdHours}시간"

private const val DefaultInactivityThresholdHours = 12
