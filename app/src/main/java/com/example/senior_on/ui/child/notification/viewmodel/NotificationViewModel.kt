package com.example.senior_on.ui.child.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.domain.repository.server.NotificationRepository
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationMessageUiState
import com.example.senior_on.ui.child.notification.NotificationScreenUiState
import com.example.senior_on.ui.child.notification.apiType
import com.example.senior_on.ui.child.notification.emptyNotificationScreenUiState
import com.example.senior_on.ui.child.notification.parentNotConnectedNotificationScreenUiState
import com.example.senior_on.ui.child.notification.toUiState
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
    val errorMessage: String? = null,
)

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val familyRepository: FamilyServerRepository?,
    private val homeRepository: HomeServerRepository?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private var inactivityTargetUserId: Long? = null

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val hasParent = familyRepository
                    ?.getMembers()
                    ?.any { member ->
                        member.role.equals(ParentRole, ignoreCase = true)
                    }
                    ?: true
                if (!hasParent) {
                    return@runCatching parentNotConnectedNotificationScreenUiState()
                }

                val home = async { repository.getHome() }
                val parentOnline = async { repository.isParentDeviceOnline() }
                val hasHomeAddress = async {
                    homeRepository
                        ?.getHome()
                        ?.seniorAddress
                        ?.isNotBlank()
                        ?: true
                }
                home.await().toUiState(
                    isParentDeviceOnline = parentOnline.await(),
                    hasHomeAddress = hasHomeAddress.await(),
                )
            }.onSuccess { home ->
                _uiState.update {
                    it.copy(isLoading = false, home = home)
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
        viewModelScope.launch {
            runCatching {
                repository.updateSetting(category.apiType, enabled)
            }.onSuccess {
                loadHome()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message)
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

    class Factory(
        private val repository: NotificationRepository,
        private val familyRepository: FamilyServerRepository?,
        private val homeRepository: HomeServerRepository?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NotificationViewModel::class.java))
            return NotificationViewModel(
                repository = repository,
                familyRepository = familyRepository,
                homeRepository = homeRepository,
            ) as T
        }
    }

    private companion object {
        const val ParentRole = "PARENT"
    }
}

private const val DefaultInactivityThresholdHours = 12
