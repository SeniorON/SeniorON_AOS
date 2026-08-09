package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val isSaving: Boolean = false,
    val currentPasswordErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    val changeCompleted: Boolean = false,
)

class ChangePasswordViewModel(
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        newPasswordCheck: String,
    ) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    currentPasswordErrorMessage = null,
                    saveErrorMessage = null,
                    changeCompleted = false,
                )
            }
            runCatching {
                val changed = userSettingsRepository.changePassword(
                    current = currentPassword,
                    new = newPassword,
                    confirmation = newPasswordCheck,
                )
                if (!changed) {
                    error("비밀번호 변경에 실패했습니다.")
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        changeCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                val message = throwable.message.orEmpty()
                val looksLikeCurrentPasswordError =
                    message.contains("비밀번호", ignoreCase = true) ||
                        message.contains("password", ignoreCase = true) ||
                        message.contains("401") ||
                        message.contains("403")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        currentPasswordErrorMessage = if (looksLikeCurrentPasswordError) {
                            "비밀번호가 일치하지 않아요."
                        } else {
                            null
                        },
                        saveErrorMessage = if (looksLikeCurrentPasswordError) {
                            null
                        } else {
                            message.ifBlank { "비밀번호 변경에 실패했습니다." }
                        },
                    )
                }
            }
        }
    }

    fun consumeCurrentPasswordError() {
        _uiState.update { it.copy(currentPasswordErrorMessage = null) }
    }

    fun consumeSaveError() {
        _uiState.update { it.copy(saveErrorMessage = null) }
    }

    fun consumeChangeCompleted() {
        _uiState.update { it.copy(changeCompleted = false) }
    }

    companion object {
        fun factory(
            userSettingsRepository: UserSettingsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ChangePasswordViewModel(userSettingsRepository) as T
        }
    }
}
