package com.example.senior_on.ui.child.display.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.PendingSeniorFamily
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
    val pendingFamilies: List<PendingSeniorFamily> = emptyList(),
    val resumeProfileFamilyId: Long? = null,
    val pendingJoinedFamilyId: Long? = null,
    val isLoading: Boolean = true,
    val isRefreshingList: Boolean = false,
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
    val awaitingRelationship: ManagedSenior? = null,
    val withdrawalNotices: List<com.example.senior_on.domain.model.senior.SeniorWithdrawalNotice> = emptyList(),
    val withdrawalDialog: com.example.senior_on.domain.model.senior.SeniorWithdrawalNotice? = null,
    val isAcknowledgingWithdrawal: Boolean = false,
    val withdrawalError: String? = null,
    val withdrawalAcknowledged: Boolean = false,
)

class SeniorManagementViewModel(
    private val seniorRepository: SeniorRepository,
    private val familyRepository: FamilyServerRepository,
    private val needsRelationship: suspend (ManagedSenior) -> Boolean = { error("담당자 관계를 확인하지 못했어요.") },
    private val accessToken: () -> String? = { null },
    private val familyRole: suspend (Long) -> CareManagerType = { error("담당자 유형을 확인하지 못했어요.") },
) : ViewModel() {
    private var refreshJob: Job? = null
    private var noticeJob: Job? = null
    private var requestedWithdrawalNoticeId: Long? = null

    fun openWithdrawalNotification(noticeId: Long?) {
        requestedWithdrawalNoticeId = noticeId
        refreshWithdrawalNotices()
    }

    fun refreshWithdrawalNotices() {
        if (_uiState.value.isAcknowledgingWithdrawal || _uiState.value.withdrawalAcknowledged) return
        noticeJob?.cancel()
        noticeJob = viewModelScope.launch {
            try {
                val notices = seniorRepository.getWithdrawalNotices()
                val requested = notices.firstOrNull { it.withdrawalNoticeId == requestedWithdrawalNoticeId }
                _uiState.update { state -> state.copy(
                    withdrawalNotices = notices,
                    withdrawalDialog = requested ?: state.withdrawalDialog,
                    managedSeniors = state.managedSeniors.map { senior ->
                        notices.firstOrNull { it.seniorId == senior.seniorId }?.let {
                            senior.copy(withdrawn = true, withdrawalNoticeId = it.withdrawalNoticeId)
                        } ?: senior
                    },
                ) }
                requestedWithdrawalNoticeId = null
                refreshSeniors()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // The ordinary list is an independent fallback for missed push messages.
                refreshSeniors()
            }
        }
    }

    fun showWithdrawal(senior: ManagedSenior) {
        if (!senior.withdrawn) return
        if (_uiState.value.isAcknowledgingWithdrawal || _uiState.value.withdrawalAcknowledged) return
        val id = senior.withdrawalNoticeId ?: return
        _uiState.update { it.copy(
            withdrawalDialog = com.example.senior_on.domain.model.senior.SeniorWithdrawalNotice(
                id, senior.familyId, senior.seniorId, senior.name, "",
            ), withdrawalError = null,
        ) }
    }

    fun acknowledgeWithdrawal(
        saveSelection: suspend (List<Long>) -> Unit,
        onSuccess: () -> Unit,
    ) {
        val notice = _uiState.value.withdrawalDialog ?: return
        if (_uiState.value.isAcknowledgingWithdrawal || _uiState.value.isDisconnecting) return
        refreshJob?.cancel()
        noticeJob?.cancel()
        _uiState.update { it.copy(isAcknowledgingWithdrawal = true, isLoading = false, withdrawalError = null) }
        viewModelScope.launch {
            try {
                if (!_uiState.value.withdrawalAcknowledged) {
                    seniorRepository.acknowledgeWithdrawal(notice.withdrawalNoticeId)
                    _uiState.update { it.copy(withdrawalAcknowledged = true) }
                }
                val families = seniorRepository.getManagedFamilies()
                val seniors = families.seniors
                saveSelection(seniors.filterNot { it.withdrawn }.map { it.seniorId })
                _uiState.update { it.copy(
                    managedSeniors = seniors,
                    pendingFamilies = families.pendingFamilies,
                    withdrawalNotices = it.withdrawalNotices.filterNot { item -> item.withdrawalNoticeId == notice.withdrawalNoticeId },
                    withdrawalDialog = null, withdrawalAcknowledged = false,
                    listError = null, errorMessage = null,
                ) }
                onSuccess()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(withdrawalError = if (it.withdrawalAcknowledged)
                    "확인은 완료됐지만 목록을 갱신하지 못했어요. 다시 시도해 주세요."
                    else "탈퇴 안내를 확인 처리하지 못했어요. 다시 시도해 주세요.") }
            } finally {
                _uiState.update { it.copy(isAcknowledgingWithdrawal = false) }
            }
        }
    }
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

    fun refreshSeniors() = loadSeniors(pullToRefresh = false)

    fun refreshEmptyList() = loadSeniors(pullToRefresh = true)

    private fun loadSeniors(pullToRefresh: Boolean) {
        if (_uiState.value.isAcknowledgingWithdrawal || _uiState.value.withdrawalAcknowledged) return
        if (pendingDisconnectRefresh != null) {
            retryDisconnectRefresh()
            return
        }
        if (_uiState.value.isDisconnecting || _uiState.value.isSubmitting) return
        if (refreshJob?.isActive == true) {
            if (pullToRefresh) _uiState.update { it.copy(isRefreshingList = it.isLoading) }
            return
        }

        refreshJob = viewModelScope.launch {
            val requestRevision = profileRevision
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isRefreshingList = pullToRefresh,
                    listError = null,
                    errorMessage = null,
                )
            }
            try {
                val families = seniorRepository.getManagedFamilies()
                val managedSeniors = families.seniors
                _uiState.update {
                    it.copy(
                        pendingFamilies = families.pendingFamilies.map { family ->
                            it.withdrawalNotices.firstOrNull { notice -> notice.familyId == family.familyId }
                                ?.let { notice -> family.copy(withdrawn = true, withdrawalNoticeId = notice.withdrawalNoticeId) }
                                ?: family
                        },
                        // A list request started before a successful edit must not restore the old label.
                        managedSeniors = managedSeniors.map { senior ->
                            val notice = it.withdrawalNotices.firstOrNull { item -> item.seniorId == senior.seniorId }
                            if (notice != null) return@map senior.copy(withdrawn = true, withdrawalNoticeId = notice.withdrawalNoticeId)
                            val saved = savedProfiles[senior.seniorId]
                            if (saved != null && saved.first > requestRevision) senior.withProfile(saved.second)
                            else senior
                        },
                        listError = null,
                        isLoading = false,
                        isRefreshingList = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                _uiState.update { it.copy(isLoading = false, isRefreshingList = false) }
                throw cancellation
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshingList = false,
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
                        pendingFamilies = state.pendingFamilies.filterNot { it.familyId == familyId },
                        resumeProfileFamilyId = null,
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
                val families = seniorRepository.getManagedFamilies()
                val seniors = families.seniors
                if (families.pendingFamilies.any { it.familyId == familyId && !it.withdrawn }) {
                    _uiState.update { it.copy(managedSeniors = seniors, pendingFamilies = families.pendingFamilies,
                        joinedFamilyId = null, awaitingRelationship = null, pendingJoinedFamilyId = familyId,
                        isLoading = false, listError = null) }
                    return@launch
                }
                val joined = seniors.firstOrNull { it.familyId == familyId && it.seniorId > 0 && !it.withdrawn }
                    ?: error("연결된 가족에 등록된 시니어가 없어요. 주담당자의 등록 후 다시 시도해 주세요.")
                val needsInput = needsRelationship(joined)
                _uiState.update { it.copy(
                    managedSeniors = seniors, pendingFamilies = families.pendingFamilies, isLoading = false, listError = null,
                    joinedFamilyId = familyId.takeIf { needsInput },
                    awaitingRelationship = joined.takeIf { needsInput }, errorMessage = null,
                ) }
                if (!needsInput) onSuccess(joined)
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

    fun saveJoinedRelationship(relationship: CaregiverRelationship, onSuccess: (ManagedSenior) -> Unit) {
        val senior = _uiState.value.awaitingRelationship ?: return
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val result = seniorRepository.updateRelation(
                    requireNotNull(accessToken()) { "로그인 상태를 확인해 주세요." }, senior.seniorId, relationship,
                )
                val updated = senior.copy(relationship = CaregiverRelationship(result.relation, result.customRelation))
                _uiState.update { it.copy(managedSeniors = it.managedSeniors.map { item ->
                    if (item.seniorId == updated.seniorId) updated else item
                }, joinedFamilyId = null, awaitingRelationship = null) }
                onSuccess(updated)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                _uiState.update { it.copy(errorMessage = failure.toSeniorManagementError("관계를 저장하지 못했어요.")) }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun clearError() {
        // Keep the read-retry prompt if the add flow is closed and reopened after a successful join.
        if (_uiState.value.joinedFamilyId != null && _uiState.value.awaitingRelationship == null) return
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Reset only navigation state. The account-scoped issued-code cache stays intact. */
    fun startFreshAddition() {
        if (_uiState.value.isSubmitting || _uiState.value.isCreatingFamilyCode) return
        _uiState.update { it.copy(joinedFamilyId = null, awaitingRelationship = null,
            createdFamilyId = null, familyCode = null, resumeProfileFamilyId = null,
            pendingJoinedFamilyId = null, errorMessage = null) }
    }

    fun consumePendingJoin() { _uiState.update { it.copy(pendingJoinedFamilyId = null) } }

    fun finishPendingRegistration(senior: ManagedSenior, onRelationship: () -> Unit, onReady: () -> Unit, onMessage: (String) -> Unit) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                if (needsRelationship(senior)) {
                    _uiState.update { it.copy(joinedFamilyId = senior.familyId, awaitingRelationship = senior,
                        resumeProfileFamilyId = null, errorMessage = null) }
                    onRelationship()
                } else onReady()
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (failure: Exception) { onMessage(failure.toSeniorManagementError())
            } finally { _uiState.update { it.copy(isSubmitting = false) } }
        }
    }

    fun resumePendingFamily(familyId: Long, onReady: () -> Unit, onMessage: (String) -> Unit) {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                when (familyRole(familyId)) {
                    CareManagerType.Primary -> {
                        _uiState.update { it.copy(createdFamilyId = familyId, familyCode = null,
                            joinedFamilyId = null, awaitingRelationship = null,
                            resumeProfileFamilyId = familyId, errorMessage = null) }
                        onReady()
                    }
                    CareManagerType.Sub -> onMessage("주담당자가 시니어 정보를 등록하고 있어요.\n등록이 완료된 후 이용할 수 있어요.")
                    else -> error("담당자 유형을 확인하지 못했어요.")
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (failure: Exception) { onMessage(failure.toSeniorManagementError())
            } finally { _uiState.update { it.copy(isSubmitting = false) } }
        }
    }

    fun showPendingWithdrawal(family: PendingSeniorFamily) {
        if (!family.withdrawn) return
        val id = family.withdrawalNoticeId ?: return
        val state = _uiState.value
        if (state.isAcknowledgingWithdrawal || state.withdrawalAcknowledged || state.withdrawalDialog != null) return
        _uiState.update { it.copy(withdrawalDialog = com.example.senior_on.domain.model.senior.SeniorWithdrawalNotice(
            id, family.familyId, null, "등록 중인 시니어", ""), withdrawalError = null) }
    }

    fun disconnectPendingFamily(familyId: Long, onSuccess: () -> Unit, onMessage: (String) -> Unit) {
        if (_uiState.value.isDisconnecting || _uiState.value.isSubmitting) return
        _uiState.update { it.copy(isDisconnecting = true) }
        viewModelScope.launch {
            try {
                refreshJob?.cancel(); refreshJob?.join()
                familyRepository.disconnectFamily(familyId)
                _uiState.update { it.copy(pendingFamilies = it.pendingFamilies.filterNot { f -> f.familyId == familyId }) }
                startFreshAddition()
                onSuccess()
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (failure: Exception) { onMessage(failure.toSeniorManagementError("연결 해제 결과를 확인하지 못했어요. 목록을 다시 확인해 주세요."))
            } finally {
                _uiState.update { it.copy(isDisconnecting = false) }
                refreshSeniors()
            }
        }
    }

    fun disconnectRelationship(
        seniorId: Long,
        validateRelationship: suspend (Long) -> Unit,
        saveSelection: suspend (List<Long>) -> Unit,
        onMessage: (String) -> Unit,
        onSuccess: () -> Unit,
        removeRelationship: suspend (Long) -> Unit,
    ) {
        if (_uiState.value.isDisconnecting || _uiState.value.isLoading || _uiState.value.isAcknowledgingWithdrawal || _uiState.value.withdrawalAcknowledged) return
        if (_uiState.value.managedSeniors.any { it.seniorId == seniorId && it.withdrawn }) {
            onMessage("탈퇴한 시니어는 탈퇴 안내를 확인해 주세요.")
            return
        }
        if (pendingDisconnectRefresh != null) {
            retryDisconnectRefresh()
            return
        }
        _uiState.update { it.copy(isDisconnecting = true) }
        viewModelScope.launch {
            try {
                // The family API checks connection history and other guardians atomically.
                // A disconnected device alone does not prove the family was never linked.
                validateRelationship(seniorId)
                removeRelationship(seniorId)
                // Once DELETE succeeds, retries must never send it again.
                pendingDisconnectRefresh = {
                    val families = seniorRepository.getManagedFamilies()
                    val seniors = families.seniors
                    saveSelection(seniors.map { it.seniorId })
                    _uiState.update { it.copy(managedSeniors = seniors, pendingFamilies = families.pendingFamilies, listError = null, errorMessage = null) }
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
        private val authRepository: com.example.senior_on.domain.repository.auth.AuthRepository,
        private val accessToken: () -> String?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SeniorManagementViewModel::class.java))
            return SeniorManagementViewModel(
                seniorRepository = seniorRepository,
                familyRepository = familyRepository,
                needsRelationship = { senior ->
                    val family = requireNotNull(authRepository.getOnboardingStatus().families.orEmpty()
                        .singleOrNull { it.familyId == senior.familyId && it.seniorId == senior.seniorId })
                    requiresRelationshipAfterCodeJoin(family.managerType)
                },
                accessToken = accessToken,
                familyRole = { familyId -> requireNotNull(authRepository.getOnboardingStatus().families.orEmpty()
                    .singleOrNull { it.familyId == familyId }).managerType },
            ) as T
        }
    }
}

internal fun requiresRelationshipAfterCodeJoin(role: CareManagerType): Boolean = when (role) {
    CareManagerType.Sub -> true
    CareManagerType.Primary -> false
    CareManagerType.None -> error("담당자 유형을 확인하지 못했어요.")
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
    "시니어 코드 생성에 실패했어요. 다시 시도해 주세요."
private const val MISSING_FAMILY_ID_MESSAGE =
    "생성된 가족 정보를 확인할 수 없어요. 코드를 다시 생성해 주세요."
private const val MISSING_FAMILY_CODE_MESSAGE =
    "생성된 시니어 코드를 확인할 수 없어요. 다시 시도해 주세요."
