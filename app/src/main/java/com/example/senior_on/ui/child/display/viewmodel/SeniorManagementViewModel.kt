package com.example.senior_on.ui.child.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.repository.senior.SeniorRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeniorManagementUiState(
    val managedSeniors: List<ManagedSenior> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingFamilyCode: Boolean = false,
    val isSubmitting: Boolean = false,
    val createdFamilyId: Long? = null,
    val familyCode: String? = null,
    val errorMessage: String? = null,
    val listError: String? = null,
    val isDisconnecting: Boolean = false,
    val disconnectRefreshPending: Boolean = false,
    val disconnectRefreshError: String? = null,
    val joinedFamilyId: Long? = null,
)

class SeniorManagementViewModel(
    private val seniorRepository: SeniorRepository,
    private val familyRepository: FamilyServerRepository,
) : ViewModel() {
    private var refreshJob: Job? = null
    private var profileRevision = 0L
    private val savedProfiles = mutableMapOf<Long, Pair<Long, com.example.senior_on.domain.model.parent.ParentInfo>>()
    private var familyCodeJob: Job? = null
    private var pendingDisconnectRefresh: (suspend () -> Unit)? = null
    private val _uiState = MutableStateFlow(SeniorManagementUiState())
    val uiState: StateFlow<SeniorManagementUiState> = _uiState.asStateFlow()

    init {
        refreshSeniors()
    }

    fun updateSavedProfile(profile: com.example.senior_on.domain.model.parent.ParentInfo) {
        savedProfiles[profile.seniorId] = ++profileRevision to profile
        _uiState.update { state ->
            state.copy(managedSeniors = state.managedSeniors.map { senior ->
                if (senior.seniorId == profile.seniorId) senior.withProfile(profile) else senior
            })
        }
    }

    private fun ManagedSenior.withProfile(profile: com.example.senior_on.domain.model.parent.ParentInfo) =
        copy(name = profile.name, relationship = CaregiverRelationship.fromDisplayLabel(profile.relationshipLabel))

    fun refreshSeniors() {
        if (pendingDisconnectRefresh != null) {
            retryDisconnectRefresh()
            return
        }
        if (refreshJob?.isActive == true || _uiState.value.isDisconnecting || _uiState.value.isSubmitting) return

        refreshJob = viewModelScope.launch {
            val requestRevision = profileRevision
            _uiState.update {
                it.copy(
                    isLoading = true,
                    listError = null,
                    errorMessage = null,
                )
            }
            try {
                val managedSeniors = seniorRepository.getManagedSeniors()
                _uiState.update {
                    it.copy(
                        // A list request started before a successful edit must not restore the old label.
                        managedSeniors = managedSeniors.map { senior ->
                            val saved = savedProfiles[senior.seniorId]
                            if (saved != null && saved.first > requestRevision) senior.withProfile(saved.second)
                            else senior
                        },
                        listError = null,
                        isLoading = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.toSeniorManagementError(),
                        listError = "시니어 목록을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    fun beginSeniorAddition() {
        val currentState = _uiState.value
        if (
            familyCodeJob?.isActive == true ||
            currentState.isSubmitting ||
            (currentState.createdFamilyId != null && !currentState.familyCode.isNullOrBlank())
        ) return

        familyCodeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCreatingFamilyCode = true,
                    errorMessage = null,
                )
            }
            try {
                val result = familyRepository.createCode()
                val familyId = requireNotNull(result.familyId) {
                    MISSING_FAMILY_ID_MESSAGE
                }
                require(result.code.isNotBlank()) {
                    MISSING_FAMILY_CODE_MESSAGE
                }
                _uiState.update {
                    it.copy(
                        isCreatingFamilyCode = false,
                        createdFamilyId = familyId,
                        familyCode = result.code,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isCreatingFamilyCode = false,
                        errorMessage = throwable.toSeniorManagementError(
                            fallback = CREATE_FAMILY_CODE_ERROR_MESSAGE,
                        ),
                    )
                }
            }
        }
    }

    fun retryCreateFamilyCode() {
        _uiState.update {
            it.copy(
                createdFamilyId = null,
                familyCode = null,
                errorMessage = null,
            )
        }
        beginSeniorAddition()
    }

    fun createSenior(
        registration: SeniorRegistration,
        onSuccess: (ManagedSenior) -> Unit,
    ) {
        if (_uiState.value.isSubmitting) return
        val familyId = _uiState.value.createdFamilyId
        if (familyId == null) {
            _uiState.update { it.copy(errorMessage = MISSING_FAMILY_ID_MESSAGE) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                )
            }
            try {
                val createdSenior = seniorRepository.createSenior(
                    familyId = familyId,
                    registration = registration,
                )
                val managedSenior = ManagedSenior(
                    familyId = familyId,
                    seniorId = createdSenior.seniorId,
                    parentUserId = null,
                    name = createdSenior.name,
                    relationship = CaregiverRelationship(
                        relation = createdSenior.relation,
                        customRelation = createdSenior.customRelation,
                    ),
                )
                _uiState.update { state ->
                    state.copy(
                        managedSeniors = state.managedSeniors.upsert(managedSenior),
                        isSubmitting = false,
                        createdFamilyId = null,
                        familyCode = null,
                    )
                }
                onSuccess(managedSenior)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.toSeniorManagementError(),
                    )
                }
            }
        }
    }

    fun joinSenior(code: String, onSuccess: (ManagedSenior) -> Unit) {
        if (_uiState.value.isSubmitting || _uiState.value.isCreatingFamilyCode) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Prevent an older list request from replacing the joined list.
                refreshJob?.cancel()
                refreshJob?.join()
                val familyId = _uiState.value.joinedFamilyId ?: requireNotNull(
                    familyRepository.join(code).familyId
                ) { "연결된 가족 정보를 확인하지 못했어요." }.also { joinedId ->
                    _uiState.update { it.copy(joinedFamilyId = joinedId) }
                }
                val seniors = seniorRepository.getManagedSeniors()
                val joined = seniors.firstOrNull { it.familyId == familyId && it.seniorId > 0 }
                    ?: error("연결된 가족에 등록된 시니어가 없어요. 주담당자의 등록 후 다시 시도해 주세요.")
                _uiState.update { it.copy(
                    managedSeniors = seniors, isLoading = false, listError = null,
                    joinedFamilyId = null, errorMessage = null,
                ) }
                onSuccess(joined)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = if (it.joinedFamilyId != null)
                        "코드 연결은 완료됐지만 시니어 정보를 확인하지 못했어요. 주담당자의 시니어 등록 여부를 확인한 후 다시 시도해 주세요."
                    else failure.message ?: "시니어 코드를 다시 확인해 주세요.",
                ) }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun clearError() {
        // Keep the read-retry prompt if the add flow is closed and reopened after a successful join.
        if (_uiState.value.joinedFamilyId != null) return
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun disconnectRelationship(
        seniorId: Long,
        isPrimary: suspend (Long) -> Boolean,
        saveSelection: suspend (List<Long>) -> Unit,
        onMessage: (String) -> Unit,
        onSuccess: () -> Unit,
        removeRelationship: suspend (Long) -> Unit,
    ) {
        if (_uiState.value.isDisconnecting || _uiState.value.isLoading) return
        if (pendingDisconnectRefresh != null) {
            retryDisconnectRefresh()
            return
        }
        _uiState.update { it.copy(isDisconnecting = true) }
        viewModelScope.launch {
            try {
                if (isPrimary(seniorId)) {
                    onMessage("주담당자는 연결을 해제할 수 없어요.")
                    return@launch
                }
                removeRelationship(seniorId)
                // Once DELETE succeeds, retries must never send it again.
                pendingDisconnectRefresh = {
                    val seniors = seniorRepository.getManagedSeniors()
                    saveSelection(seniors.map { it.seniorId })
                    _uiState.update { it.copy(managedSeniors = seniors, listError = null, errorMessage = null) }
                    onSuccess()
                }
                _uiState.update { it.copy(disconnectRefreshPending = true) }
                completeDisconnectRefresh()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                onMessage(error.message ?: "연결 해제 요청에 실패했어요. 다시 시도해 주세요.")
            } finally {
                _uiState.update { it.copy(isDisconnecting = false) }
            }
        }
    }

    fun retryDisconnectRefresh() {
        if (_uiState.value.isDisconnecting || pendingDisconnectRefresh == null) return
        _uiState.update { it.copy(isDisconnecting = true) }
        viewModelScope.launch {
            try {
                completeDisconnectRefresh()
            } finally {
                _uiState.update { it.copy(isDisconnecting = false) }
            }
        }
    }

    private suspend fun completeDisconnectRefresh() {
        _uiState.update { it.copy(disconnectRefreshError = null) }
        try {
            pendingDisconnectRefresh?.invoke()
            pendingDisconnectRefresh = null
            _uiState.update { it.copy(disconnectRefreshPending = false) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            _uiState.update {
                it.copy(disconnectRefreshError = "연결 해제는 완료됐지만 목록을 갱신하지 못했어요. 다시 시도해 주세요.")
            }
        }
    }

    class Factory(
        private val seniorRepository: SeniorRepository,
        private val familyRepository: FamilyServerRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SeniorManagementViewModel::class.java))
            return SeniorManagementViewModel(
                seniorRepository = seniorRepository,
                familyRepository = familyRepository,
            ) as T
        }
    }
}

private fun List<ManagedSenior>.upsert(senior: ManagedSenior): List<ManagedSenior> =
    (filterNot { it.seniorId == senior.seniorId } + senior)
        .sortedBy(ManagedSenior::seniorId)

private fun Throwable.toSeniorManagementError(
    fallback: String = DEFAULT_ERROR_MESSAGE,
): String = message?.takeIf(String::isNotBlank) ?: fallback

private const val DEFAULT_ERROR_MESSAGE =
    "시니어 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요."
private const val CREATE_FAMILY_CODE_ERROR_MESSAGE =
    "가족 공유 코드 생성에 실패했어요. 다시 시도해 주세요."
private const val MISSING_FAMILY_ID_MESSAGE =
    "생성된 가족 정보를 확인할 수 없어요. 코드를 다시 생성해 주세요."
private const val MISSING_FAMILY_CODE_MESSAGE =
    "생성된 가족 공유 코드를 확인할 수 없어요. 다시 시도해 주세요."
