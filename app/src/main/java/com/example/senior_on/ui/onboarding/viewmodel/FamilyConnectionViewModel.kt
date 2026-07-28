package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FamilyConnectionUiState(
    val isLoading: Boolean = false,
    val familyCode: String? = null,
    val errorMessage: String? = null,
)

class FamilyConnectionViewModel(
    private val repository: FamilyServerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FamilyConnectionUiState())
    val uiState: StateFlow<FamilyConnectionUiState> = _uiState.asStateFlow()

    fun joinFamily(
        familyCode: String,
        onSuccess: (FamilyCodeInfo) -> Unit
    ) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = FamilyConnectionUiState(isLoading = true)
            runCatching { repository.join(familyCode) }
                .onSuccess { result ->
                    _uiState.value = FamilyConnectionUiState()
                    onSuccess(result)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: INVALID_FAMILY_CODE_MESSAGE
                    )
                }
        }
    }

    fun createFamilyCode() {
        if (_uiState.value.isLoading || _uiState.value.familyCode != null) return
        viewModelScope.launch {
            _uiState.value = FamilyConnectionUiState(isLoading = true)
            runCatching { repository.createCode() }
                .onSuccess { result ->
                    _uiState.value = FamilyConnectionUiState(
                        familyCode = result.code
                    )
                }
                .onFailure {
                    _uiState.value = FamilyConnectionUiState(
                        errorMessage = it.message ?: CREATE_FAMILY_CODE_ERROR_MESSAGE
                    )
                }
        }
    }

    fun retryCreateFamilyCode() {
        _uiState.value = FamilyConnectionUiState()
        createFamilyCode()
    }

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    class Factory(
        private val repository: FamilyServerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FamilyConnectionViewModel::class.java))
            return FamilyConnectionViewModel(repository) as T
        }
    }

    private companion object {
        const val CREATE_FAMILY_CODE_ERROR_MESSAGE =
            "가족 공유 코드 생성에 실패했어요. 다시 시도해 주세요."
        const val INVALID_FAMILY_CODE_MESSAGE =
            "가족 공유 코드를 다시 확인해 주세요."
    }
}
