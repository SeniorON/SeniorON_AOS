package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.RemoteRequestException
import com.example.senior_on.domain.model.auth.FoundLoginId
import com.example.senior_on.domain.repository.auth.AccountRecoveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AccountRecoveryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val verificationId: Long? = null,
    val foundLoginId: String = "",
    val foundJoinDate: String = "",
    val recoveryName: String = "",
    val passwordResetVerified: Boolean = false,
    val passwordResetComplete: Boolean = false,
    val requestLoginId: String = "",
    val errorCode: String? = null,
)

class AccountRecoveryViewModel(
    private val repository: AccountRecoveryRepository,
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000 },
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountRecoveryUiState())
    val uiState: StateFlow<AccountRecoveryUiState> = _uiState.asStateFlow()

    private var passwordResetName: String = ""
    private var passwordResetLoginId: String = ""
    private var requestJob: Job? = null
    // Delivery metadata only: never retain the entered code, password or verified state.
    private data class PendingDelivery(
        val name: String,
        val loginId: String,
        val verificationId: Long,
        val expiresAt: Long,
    )
    private var pendingDelivery: PendingDelivery? = null

    fun cancelPendingRequest() {
        requestJob?.cancel()
        requestJob = null
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun resetFlow() {
        cancelPendingRequest()
        passwordResetName = ""
        passwordResetLoginId = ""
        _uiState.value = AccountRecoveryUiState()
    }

    fun clearRequestError() {
        _uiState.value = _uiState.value.copy(errorMessage = null, errorCode = null)
    }

    fun findLoginId(
        name: String,
        email: String,
        onResult: (FoundLoginId?) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(foundLoginId = "", foundJoinDate = "", recoveryName = "")
        launchRequest(onFailure = { onResult(null) }) {
            val result = repository.findLoginId(name = name, email = email)
            currentCoroutineContext().ensureActive()
            _uiState.value = _uiState.value.copy(
                foundLoginId = result.loginId,
                foundJoinDate = result.createdAt.toJoinDate(),
                recoveryName = name.trim()
            )
            onResult(result)
        }
    }

    fun continuePasswordRecovery(name: String, loginId: String, onResult: (Boolean) -> Unit) {
        val pending = pendingDelivery?.takeIf { it.expiresAt > nowMillis() }
        if (pending == null) pendingDelivery = null
        // A new UI flow can still use an unexpired email sent for exactly this identity.
        if (pending != null && pending.name == name.trim() && pending.loginId == loginId.trim()) {
            cancelPendingRequest()
            passwordResetName = pending.name
            passwordResetLoginId = pending.loginId
            _uiState.value = AccountRecoveryUiState(
                requestLoginId = pending.loginId,
                verificationId = pending.verificationId,
            )
            onResult(true)
        } else {
            sendPasswordResetVerificationCode(name, loginId, onResult)
        }
    }

    fun sendPasswordResetVerificationCode(
        name: String,
        loginId: String,
        onResult: (Boolean) -> Unit
    ) {
        passwordResetName = ""
        passwordResetLoginId = ""
        _uiState.value = _uiState.value.copy(
            requestLoginId = loginId.trim(), verificationId = null,
            passwordResetVerified = false, passwordResetComplete = false,
        )
        launchRequest(onFailure = { onResult(false) }) {
            val result = repository.sendPasswordResetVerificationCode(
                name = name,
                loginId = loginId
            )
            currentCoroutineContext().ensureActive()
            if (!result.sent) {
                onResult(false)
                return@launchRequest
            }
            passwordResetName = name.trim()
            passwordResetLoginId = loginId.trim()
            pendingDelivery = PendingDelivery(
                name = passwordResetName,
                loginId = passwordResetLoginId,
                verificationId = result.verificationId,
                // Matches the five-minute delivery lifetime in VerificationRequestGate.
                expiresAt = nowMillis() + 300_000,
            )
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

        _uiState.value = _uiState.value.copy(passwordResetVerified = false)
        launchRequest(onFailure = { onResult(false) }) {
            val verified = repository.verifyPasswordResetVerificationCode(
                verificationId = verificationId,
                verificationCode = verificationCode
            )
            currentCoroutineContext().ensureActive()
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
            currentCoroutineContext().ensureActive()
            if (reset) pendingDelivery = null
            _uiState.value = _uiState.value.copy(
                passwordResetComplete = reset,
                verificationId = if (reset) null else verificationId,
                passwordResetVerified = !reset && _uiState.value.passwordResetVerified,
            )
            onResult(reset)
        }
    }

    private fun launchRequest(
        onFailure: () -> Unit,
        errorText: (Throwable) -> String = { it.message ?: DEFAULT_ERROR_MESSAGE },
        request: suspend () -> Unit
    ) {
        cancelPendingRequest()
        requestJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                errorCode = null,
            )
            runCatching { request() }
                .onSuccess {
                    currentCoroutineContext().ensureActive()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { throwable ->
                    currentCoroutineContext().ensureActive()
                    if (throwable is CancellationException) throw throwable
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorText(throwable),
                        errorCode = (throwable as? RemoteRequestException)?.code,
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

private val JOIN_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private fun String.toJoinDate(): String =
    runCatching {
        LocalDateTime.parse(trim()).toLocalDate().format(JOIN_DATE_FORMATTER)
    }.getOrDefault("")
