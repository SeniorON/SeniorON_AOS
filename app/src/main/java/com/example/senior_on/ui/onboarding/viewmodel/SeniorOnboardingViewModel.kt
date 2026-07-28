package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.senior.SeniorInfo
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.model.senior.SeniorRelationUpdate
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.senior.SeniorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class SeniorOnboardingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registeredSenior: SeniorInfo? = null,
    val parentInfo: ParentInfo? = null
)

class SeniorOnboardingViewModel(
    private val seniorRepository: SeniorRepository,
    private val parentInfoRepository: ParentInfoRepository,
    private val caregiverRelationshipRepositoryFor:
        (userId: String) -> CaregiverRelationshipRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeniorOnboardingUiState())
    val uiState: StateFlow<SeniorOnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            parentInfoRepository.parentInfo.collect { parentInfo ->
                _uiState.value = _uiState.value.copy(parentInfo = parentInfo)
            }
        }
    }

    fun createSenior(
        accessToken: String,
        registration: SeniorRegistration,
        parentInfo: ParentInfo,
        onResult: (SeniorInfo?) -> Unit
    ) {
        launchRequest(onFailure = { onResult(null) }) {
            val result = seniorRepository.createSenior(
                accessToken = accessToken,
                registration = registration
            )
            parentInfoRepository.saveParentInfo(
                parentInfo.copy(seniorId = result.seniorId)
            )
            _uiState.value = _uiState.value.copy(registeredSenior = result)
            onResult(result)
        }
    }

    fun updateRelation(
        accessToken: String,
        userId: String,
        seniorId: Long,
        relationship: CaregiverRelationship,
        onResult: (SeniorRelationUpdate?) -> Unit
    ) {
        launchRequest(onFailure = { onResult(null) }) {
            val result = seniorRepository.updateRelation(
                accessToken = accessToken,
                seniorId = seniorId,
                relationship = relationship
            )
            caregiverRelationshipRepositoryFor(userId).saveRelationship(
                seniorId = seniorId,
                relationship = relationship
            )
            onResult(result)
        }
    }

    private fun launchRequest(
        onFailure: () -> Unit,
        request: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            runCatching { request() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: DEFAULT_ERROR_MESSAGE
                    )
                    onFailure()
                }
        }
    }

    class Factory(
        private val seniorRepository: SeniorRepository,
        private val parentInfoRepository: ParentInfoRepository,
        private val caregiverRelationshipRepositoryFor:
            (userId: String) -> CaregiverRelationshipRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SeniorOnboardingViewModel::class.java))
            return SeniorOnboardingViewModel(
                seniorRepository = seniorRepository,
                parentInfoRepository = parentInfoRepository,
                caregiverRelationshipRepositoryFor =
                    caregiverRelationshipRepositoryFor
            ) as T
        }
    }

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "시니어 정보를 저장하지 못했습니다."
    }
}
