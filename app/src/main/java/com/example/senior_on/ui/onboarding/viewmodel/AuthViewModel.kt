package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.model.auth.KakaoLoginResult
import com.example.senior_on.domain.model.auth.LoginCredentials
import com.example.senior_on.domain.model.auth.LoginResult
import com.example.senior_on.domain.model.auth.SignupCredentials
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.auth.SocialAuthRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signupEmailRequestErrorMessage: String? = null,
)

data class SignupDraft(
    val name: String = "",
    val birth: String = "",
    val email: String = "",
    val loginId: String = "",
    val password: String = "",
    val passwordCheck: String = ""
)

data class SignupAgreements(
    val serviceTerms: Boolean,
    val privacyPolicy: Boolean,
    val ageOver14: Boolean,
    val marketing: Boolean
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val socialAuthRepository: SocialAuthRepository,
    private val sessionRepository: SessionRepository,
    private val deviceRegistrationRepository: DeviceRegistrationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var signupDraft = SignupDraft()

    var accessToken: String? = null
        private set

    fun loadSavedSession(onResult: (AuthSession?) -> Unit) {
        launchRequest {
            onResult(sessionRepository.validateSavedSession())
        }
    }

    fun login(
        loginId: String,
        password: String,
        mode: AppUserMode,
        onResult: (LoginResult?) -> Unit
    ) {
        launchRequest(
            onFailure = { _ -> onResult(null) }
        ) {
            val deviceRegistration =
                deviceRegistrationRepository.getDeviceRegistration()
            val result = authRepository.login(
                LoginCredentials(
                    loginId = loginId,
                    password = password,
                    fcmToken = deviceRegistration.fcmToken,
                    deviceIdentifier = deviceRegistration.deviceIdentifier
                )
            )
            accessToken = result?.accessToken
            if (result != null) {
                sessionRepository.saveSession(
                    accessToken = result.accessToken,
                    userId = result.loginId,
                    mode = mode,
                )
            }
            onResult(result)
        }
    }

    fun saveNameAndBirth(name: String, birth: String) {
        signupDraft = signupDraft.copy(
            name = name.trim(),
            birth = birth.replace('.', '-')
        )
    }

    fun saveVerifiedEmail(email: String) {
        signupDraft = signupDraft.copy(email = email.trim())
    }

    fun saveAccountInfo(
        loginId: String,
        password: String,
        passwordCheck: String
    ) {
        signupDraft = signupDraft.copy(
            loginId = loginId.trim(),
            password = password,
            passwordCheck = passwordCheck
        )
    }

    fun sendSignupEmailVerificationCode(
        email: String,
        onResult: (Boolean) -> Unit
    ) {
        launchBooleanRequest(
            onResult = onResult,
            onError = { message ->
                _uiState.update {
                    it.copy(signupEmailRequestErrorMessage = message)
                }
            },
        ) {
            authRepository.sendSignupEmailVerificationCode(email)
        }
    }

    fun verifySignupEmailVerificationCode(
        email: String,
        verificationCode: String,
        onResult: (Boolean) -> Unit
    ) {
        launchBooleanRequest(onResult) {
            authRepository.verifySignupEmailVerificationCode(
                email = email,
                verificationCode = verificationCode
            )
        }
    }

    fun checkLoginId(
        loginId: String,
        onResult: (Boolean?) -> Unit
    ) {
        launchRequest(
            onFailure = { _ -> onResult(null) }
        ) {
            onResult(authRepository.isLoginIdAvailable(loginId))
        }
    }

    fun completeSignup(
        mode: AppUserMode,
        agreements: SignupAgreements,
        onResult: (LoginResult?) -> Unit
    ) {
        launchRequest(
            onFailure = { _ -> onResult(null) }
        ) {
            val draft = signupDraft
            authRepository.signup(
                SignupCredentials(
                    loginId = draft.loginId,
                    email = draft.email,
                    password = draft.password,
                    passwordCheck = draft.passwordCheck,
                    name = draft.name,
                    birth = draft.birth,
                    agreeServiceTerms = agreements.serviceTerms,
                    agreePrivacyPolicy = agreements.privacyPolicy,
                    agreeAgeOver14 = agreements.ageOver14,
                    agreeMarketing = agreements.marketing
                )
            )

            val deviceRegistration =
                deviceRegistrationRepository.getDeviceRegistration()
            val loginResult = requireNotNull(
                authRepository.login(
                    LoginCredentials(
                        loginId = draft.loginId,
                        password = draft.password,
                        fcmToken = deviceRegistration.fcmToken,
                        deviceIdentifier = deviceRegistration.deviceIdentifier
                    )
                )
            )
            authRepository.updateRole(
                accessToken = loginResult.accessToken,
                mode = mode
            )

            accessToken = loginResult.accessToken
            sessionRepository.saveSession(
                accessToken = loginResult.accessToken,
                userId = loginResult.loginId,
                mode = mode,
            )
            signupDraft = SignupDraft()
            onResult(loginResult.copy(mode = mode))
        }
    }

    fun loginWithKakao(
        kakaoAccessToken: String,
        onResult: (KakaoLoginResult?) -> Unit
    ) {
        launchRequest(
            onFailure = { _ -> onResult(null) }
        ) {
            val result = socialAuthRepository.loginWithKakao(kakaoAccessToken)
            accessToken = result.accessToken
            result.mode?.let { mode ->
                sessionRepository.saveSession(
                    accessToken = result.accessToken,
                    userId = result.usersId.toString(),
                    mode = mode,
                )
            }
            onResult(result)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSignupEmailRequestError() {
        _uiState.update {
            it.copy(signupEmailRequestErrorMessage = null)
        }
    }

    private fun launchBooleanRequest(
        onResult: (Boolean) -> Unit,
        onError: (String) -> Unit = {},
        request: suspend () -> Boolean
    ) {
        launchRequest(
            onFailure = { message ->
                onError(message)
                onResult(false)
            }
        ) {
            onResult(request())
        }
    }

    private fun launchRequest(
        onFailure: (String) -> Unit = {},
        request: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { request() }
                .onSuccess {
                    _uiState.value = AuthUiState()
                }
                .onFailure { throwable ->
                    val errorMessage = throwable.message
                        ?: DEFAULT_ERROR_MESSAGE
                    _uiState.value = AuthUiState(
                        errorMessage = errorMessage
                    )
                    onFailure(errorMessage)
                }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val socialAuthRepository: SocialAuthRepository,
        private val sessionRepository: SessionRepository,
        private val deviceRegistrationRepository: DeviceRegistrationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AuthViewModel::class.java))
            return AuthViewModel(
                authRepository = authRepository,
                socialAuthRepository = socialAuthRepository,
                sessionRepository = sessionRepository,
                deviceRegistrationRepository = deviceRegistrationRepository
            ) as T
        }
    }

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "요청 처리 중 문제가 발생했습니다."
    }
}
