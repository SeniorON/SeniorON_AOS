package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoggingOut: Boolean = false,
    val logoutErrorMessage: String? = null,
    val logoutCompleted: Boolean = false,
    val isWithdrawing: Boolean = false,
    val withdrawErrorMessage: String? = null,
    val withdrawCompleted: Boolean = false,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val deviceRegistrationRepository: DeviceRegistrationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun logout() {
        if (_uiState.value.isLoggingOut) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggingOut = true,
                    logoutErrorMessage = null,
                    logoutCompleted = false,
                )
            }
            runCatching {
                val deviceIdentifier = deviceRegistrationRepository
                    .getDeviceRegistration()
                    .deviceIdentifier
                runCatching {
                    authRepository.logout(deviceIdentifier)
                }
                sessionRepository.clearSession()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        logoutCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoggingOut = false,
                        logoutErrorMessage = throwable.message
                            ?: "로그아웃에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun withdraw() {
        if (_uiState.value.isWithdrawing) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isWithdrawing = true,
                    withdrawErrorMessage = null,
                    withdrawCompleted = false,
                )
            }
            runCatching {
                authRepository.withdraw()
            }.onSuccess {
                runCatching { sessionRepository.clearSession() }
                _uiState.update {
                    it.copy(
                        isWithdrawing = false,
                        withdrawCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isWithdrawing = false,
                        withdrawErrorMessage = throwable.message
                            ?: "회원 탈퇴에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun consumeLogoutError() {
        _uiState.update { it.copy(logoutErrorMessage = null) }
    }

    fun consumeLogoutCompleted() {
        _uiState.update { it.copy(logoutCompleted = false) }
    }

    fun consumeWithdrawError() {
        _uiState.update { it.copy(withdrawErrorMessage = null) }
    }

    fun consumeWithdrawCompleted() {
        _uiState.update { it.copy(withdrawCompleted = false) }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            sessionRepository: SessionRepository,
            deviceRegistrationRepository: DeviceRegistrationRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(
                    authRepository = authRepository,
                    sessionRepository = sessionRepository,
                    deviceRegistrationRepository = deviceRegistrationRepository,
                ) as T
        }
    }
}
