package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.FoundLoginId
import com.example.senior_on.domain.repository.auth.AccountRecoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountRecoveryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val verificationId: Long? = null,
    val foundLoginId: String = "",
    val recoveryName: String = "",
    val passwordResetVerified: Boolean = false,
    val passwordResetComplete: Boolean = false
)

class AccountRecoveryViewModel(
    private val repository: AccountRecoveryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountRecoveryUiState())
    val uiState: StateFlow<AccountRecoveryUiState> = _uiState.asStateFlow()

    private var passwordResetName: String = ""
    private var passwordResetLoginId: String = ""

    fun findLoginId(
        name: String,
        email: String,
        onResult: (FoundLoginId?) -> Unit
    ) {
        launchRequest(onFailure = { onResult(null) }) {
            val result = repository.findLoginId(name = name, email = email)
            _uiState.value = _uiState.value.copy(
                foundLoginId = result.loginId,
                recoveryName = name.trim()
            )
            onResult(result)
        }
    }

    fun sendPasswordResetVerificationCode(
        name: String,
        loginId: String,
        onResult: (Boolean) -> Unit
    ) {
        launchRequest(onFailure = { onResult(false) }) {
            val result = repository.sendPasswordResetVerificationCode(
                name = name,
                loginId = loginId
            )
            passwordResetName = name.trim()
            passwordResetLoginId = loginId.trim()
            _uiState.value = _uiState.value.copy(
                verificationId = result.verificationId,
                passwordResetVerified = false,
                passwordResetComplete = false
            )
            onResult(result.sent)
        }
    }

    fun resendPasswordResetVerificationCode(
        onResult: (Boolean) -> Unit = {}
    ) {
        if (passwordResetName.isBlank() || passwordResetLoginId.isBlank()) {
            onResult(false)
            return
        }
        sendPasswordResetVerificationCode(
            name = passwordResetName,
            loginId = passwordResetLoginId,
            onResult = onResult
        )
    }

    fun verifyPasswordResetCode(
        verificationCode: String,
        onResult: (Boolean) -> Unit
    ) {
        val verificationId = _uiState.value.verificationId
        if (verificationId == null) {
            onResult(false)
            return
        }

        launchRequest(onFailure = { onResult(false) }) {
            val verified = repository.verifyPasswordResetVerificationCode(
                verificationId = verificationId,
                verificationCode = verificationCode
            )
            _uiState.value = _uiState.value.copy(
                passwordResetVerified = verified
            )
            onResult(verified)
        }
    }

    fun resetPassword(
        newPassword: String,
        newPasswordCheck: String,
        onResult: (Boolean) -> Unit
    ) {
        val verificationId = _uiState.value.verificationId
        if (verificationId == null || !_uiState.value.passwordResetVerified) {
            onResult(false)
            return
        }

        launchRequest(onFailure = { onResult(false) }) {
            val reset = repository.resetPassword(
                verificationId = verificationId,
                newPassword = newPassword,
                newPasswordCheck = newPasswordCheck
            )
            _uiState.value = _uiState.value.copy(passwordResetComplete = reset)
            onResult(reset)
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
        private val repository: AccountRecoveryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AccountRecoveryViewModel::class.java))
            return AccountRecoveryViewModel(repository) as T
        }
    }

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "계정 정보를 확인하지 못했습니다."
    }
}
