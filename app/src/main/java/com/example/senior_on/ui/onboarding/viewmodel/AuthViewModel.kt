package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.domain.model.auth.SocialLoginResult
import com.example.senior_on.domain.model.auth.SocialProvider
import com.example.senior_on.domain.model.auth.SocialSignupCredentials
import com.example.senior_on.domain.model.auth.LoginCredentials
import com.example.senior_on.domain.model.auth.LoginResult
import com.example.senior_on.domain.model.auth.OnboardingStatus
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

private data class PendingSocialSignup(
    val provider: SocialProvider,
    val socialToken: String,
    val keepLoggedIn: Boolean,
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
    private var pendingSocialSignup: PendingSocialSignup? = null

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
        keepLoggedIn: Boolean,
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
            when {
                result == null || result.mode == null -> {
                    accessToken = null
                    sessionRepository.clearSession()
                    onResult(null)
                }
                result.mode != mode -> {
                    accessToken = null
                    sessionRepository.clearSession()
                    onResult(result)
                }
                else -> {
                    accessToken = result.accessToken
                    sessionRepository.saveLoginSession(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        deviceIdentifier = deviceRegistration.deviceIdentifier,
                        userId = result.loginId,
                        mode = result.mode,
                        keepLoggedIn = shouldPersistSession(mode, keepLoggedIn),
                    )
                    onResult(result)
                }
            }
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
            pendingSocialSignup?.let { pendingSocial ->
                onResult(
                    completeSocialSignup(
                        pendingSocial = pendingSocial,
                        mode = mode,
                        agreements = agreements,
                    )
                )
                return@launchRequest
            }
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
            sessionRepository.saveLoginSession(
                accessToken = loginResult.accessToken,
                refreshToken = loginResult.refreshToken,
                deviceIdentifier = deviceRegistration.deviceIdentifier,
                userId = loginResult.loginId,
                mode = mode,
                keepLoggedIn = true,
            )
            signupDraft = SignupDraft()
            onResult(loginResult.copy(mode = mode))
        }
    }

    fun loadOnboardingStatus(onResult: (OnboardingStatus?) -> Unit) {
        launchRequest(onFailure = { onResult(null) }) {
            onResult(authRepository.getOnboardingStatus())
        }
    }

    private suspend fun completeSocialSignup(
        pendingSocial: PendingSocialSignup,
        mode: AppUserMode,
        agreements: SignupAgreements,
    ): LoginResult {
        val draft = signupDraft
        val deviceRegistration =
            deviceRegistrationRepository.getDeviceRegistration()
        val socialResult = socialAuthRepository.signup(
            SocialSignupCredentials(
                provider = pendingSocial.provider,
                socialToken = pendingSocial.socialToken,
                name = draft.name,
                birth = draft.birth,
                mode = mode,
                serviceTermsAgreed = agreements.serviceTerms,
                privacyPolicyAgreed = agreements.privacyPolicy,
                ageOver14Agreed = agreements.ageOver14,
                marketingAgreed = agreements.marketing,
                fcmToken = deviceRegistration.fcmToken,
                deviceIdentifier = deviceRegistration.deviceIdentifier,
            )
        )
        val resultAccessToken = requireNotNull(socialResult.accessToken) {
            "소셜 회원가입 응답에 액세스 토큰이 없습니다."
        }
        val resultUsersId = requireNotNull(socialResult.usersId) {
            "소셜 회원가입 응답에 사용자 ID가 없습니다."
        }
        val resultMode = requireNotNull(socialResult.mode) {
            "소셜 회원가입 응답에 사용자 역할이 없습니다."
        }

        accessToken = resultAccessToken
        sessionRepository.saveLoginSession(
            accessToken = resultAccessToken,
            refreshToken = socialResult.refreshToken,
            deviceIdentifier = deviceRegistration.deviceIdentifier,
            userId = resultUsersId.toString(),
            mode = resultMode,
            keepLoggedIn = pendingSocial.keepLoggedIn,
        )
        pendingSocialSignup = null
        signupDraft = SignupDraft()
        return LoginResult(
            usersId = resultUsersId,
            name = socialResult.name,
            loginId = resultUsersId.toString(),
            accessToken = resultAccessToken,
            mode = resultMode,
            refreshToken = socialResult.refreshToken,
        )
    }

    fun loginWithKakao(
        kakaoAccessToken: String,
        mode: AppUserMode,
        keepLoggedIn: Boolean,
        onResult: (SocialLoginResult?) -> Unit,
    ) {
        loginWithSocial(
            provider = SocialProvider.Kakao,
            socialToken = kakaoAccessToken,
            mode = mode,
            keepLoggedIn = keepLoggedIn,
            onResult = onResult,
        ) { fcmToken, deviceIdentifier ->
            socialAuthRepository.loginWithKakao(
                kakaoAccessToken = kakaoAccessToken,
                fcmToken = fcmToken,
                deviceIdentifier = deviceIdentifier,
            )
        }
    }

    fun loginWithGoogle(
        firebaseIdToken: String,
        mode: AppUserMode,
        keepLoggedIn: Boolean,
        onResult: (SocialLoginResult?) -> Unit,
    ) {
        loginWithSocial(
            provider = SocialProvider.Google,
            socialToken = firebaseIdToken,
            mode = mode,
            keepLoggedIn = keepLoggedIn,
            onResult = onResult,
        ) { fcmToken, deviceIdentifier ->
            socialAuthRepository.loginWithGoogle(
                firebaseIdToken = firebaseIdToken,
                fcmToken = fcmToken,
                deviceIdentifier = deviceIdentifier,
            )
        }
    }

    fun hasPendingSocialSignup(): Boolean = pendingSocialSignup != null

    fun resetSignupFlow() {
        signupDraft = SignupDraft()
        pendingSocialSignup = null
        _uiState.value = AuthUiState()
    }

    private fun loginWithSocial(
        provider: SocialProvider,
        socialToken: String,
        mode: AppUserMode,
        keepLoggedIn: Boolean,
        onResult: (SocialLoginResult?) -> Unit,
        request: suspend (
            fcmToken: String,
            deviceIdentifier: String,
        ) -> SocialLoginResult,
    ) {
        launchRequest(onFailure = { _ -> onResult(null) }) {
            val deviceRegistration =
                deviceRegistrationRepository.getDeviceRegistration()
            val result = request(
                deviceRegistration.fcmToken,
                deviceRegistration.deviceIdentifier,
            )

            if (result.isNewUser) {
                pendingSocialSignup = PendingSocialSignup(
                    provider = provider,
                    socialToken = socialToken,
                    keepLoggedIn = shouldPersistSession(mode, keepLoggedIn),
                )
                signupDraft = SignupDraft(name = result.name)
                accessToken = null
                onResult(result)
                return@launchRequest
            }

            pendingSocialSignup = null
            val resultMode = requireNotNull(result.mode) {
                "소셜 로그인 응답에 사용자 역할이 없습니다."
            }
            val resultAccessToken = requireNotNull(result.accessToken) {
                "소셜 로그인 응답에 액세스 토큰이 없습니다."
            }
            val resultUsersId = requireNotNull(result.usersId) {
                "소셜 로그인 응답에 사용자 ID가 없습니다."
            }

            if (resultMode == mode) {
                accessToken = resultAccessToken
                sessionRepository.saveLoginSession(
                    accessToken = resultAccessToken,
                    refreshToken = result.refreshToken,
                    deviceIdentifier = deviceRegistration.deviceIdentifier,
                    userId = resultUsersId.toString(),
                    mode = resultMode,
                    keepLoggedIn = shouldPersistSession(mode, keepLoggedIn),
                )
            } else {
                accessToken = null
                sessionRepository.clearSession()
            }
            onResult(result)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun shouldPersistSession(
        mode: AppUserMode,
        requestedByUser: Boolean,
    ): Boolean = requestedByUser || mode == AppUserMode.Senior

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
