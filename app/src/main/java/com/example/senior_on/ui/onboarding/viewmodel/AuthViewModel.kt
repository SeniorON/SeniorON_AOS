package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.auth.RemoteRequestException
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signupEmailRequestErrorMessage: String? = null,
    val signupVerificationErrorMessage: String? = null,
    val errorCode: String? = null,
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
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val socialAuthRepository: SocialAuthRepository,
    private val sessionRepository: SessionRepository,
    private val deviceRegistrationRepository: DeviceRegistrationRepository,
    private val currentAccessToken: () -> String? = {
        com.example.senior_on.data.local.AccessTokenStore.getBearerToken()?.removePrefix("Bearer ")
    },
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var signupDraft = SignupDraft()
    private var pendingSocialSignup: PendingSocialSignup? = null
    private var requestJob: Job? = null

    fun cancelPendingRequest() {
        requestJob?.cancel()
        requestJob = null
        _uiState.value = AuthUiState()
    }

    var authenticatedUsersId: Long? = null
        private set

    var accessToken: String? = null
        private set

    fun loadSavedSession(onResult: (AuthSession?) -> Unit) {
        launchRequest {
            val session = sessionRepository.validateSavedSession()
            currentCoroutineContext().ensureActive()
            authenticatedUsersId = session?.usersId
            // Post-login registration routes also check this ViewModel's token before saving.
            accessToken = if (session != null) currentAccessToken() else null
            onResult(session)
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
            currentCoroutineContext().ensureActive()
            val result = authRepository.login(
                LoginCredentials(
                    loginId = loginId,
                    password = password,
                    fcmToken = deviceRegistration.fcmToken,
                    deviceIdentifier = deviceRegistration.deviceIdentifier
                )
            )
            currentCoroutineContext().ensureActive()
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
                    authenticatedUsersId = result.usersId
                    sessionRepository.saveLoginSession(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken,
                        deviceIdentifier = deviceRegistration.deviceIdentifier,
                        userId = result.loginId,
                        usersId = result.usersId,
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
        launchBooleanRequest(onResult, onError = { message ->
            _uiState.update { it.copy(signupVerificationErrorMessage = signupVerificationErrorText(it.errorCode, message)) }
        }) {
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
            val available = authRepository.isLoginIdAvailable(loginId)
            currentCoroutineContext().ensureActive()
            onResult(available)
        }
    }

    fun completeSignup(
        mode: AppUserMode,
        agreements: SignupAgreements,
        onResult: (Boolean) -> Unit
    ) {
        launchRequest(
            onFailure = { _ -> onResult(false) }
        ) {
            pendingSocialSignup?.let { pendingSocial ->
                completeSocialSignup(
                    pendingSocial = pendingSocial,
                    mode = mode,
                    agreements = agreements,
                )
                currentCoroutineContext().ensureActive()
                finishSignupWithoutSession()
                onResult(true)
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
                    mode = mode,
                    agreeServiceTerms = agreements.serviceTerms,
                    agreePrivacyPolicy = agreements.privacyPolicy,
                    agreeAgeOver14 = agreements.ageOver14,
                    agreeMarketing = agreements.marketing
                )
            )
            currentCoroutineContext().ensureActive()
            finishSignupWithoutSession()
            onResult(true)
        }
    }

    fun loadOnboardingStatus(
        onResult: (OnboardingStatus) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        launchRequest(onFailure = onFailure) {
            val status = authRepository.getOnboardingStatus()
            currentCoroutineContext().ensureActive()
            onResult(status)
        }
    }

    fun clearSession() {
        authenticatedUsersId = null
        accessToken = null
        sessionRepository.clearSession()
    }

    private suspend fun completeSocialSignup(
        pendingSocial: PendingSocialSignup,
        mode: AppUserMode,
        agreements: SignupAgreements,
    ) {
        val draft = signupDraft
        val deviceRegistration =
            deviceRegistrationRepository.getDeviceRegistration()
        socialAuthRepository.signup(
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
    }

    private fun finishSignupWithoutSession() {
        authenticatedUsersId = null
        accessToken = null
        sessionRepository.clearSession()
        pendingSocialSignup = null
        signupDraft = SignupDraft()
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
        cancelPendingRequest()
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
            currentCoroutineContext().ensureActive()

            if (result.isNewUser) {
                pendingSocialSignup = PendingSocialSignup(
                    provider = provider,
                    socialToken = socialToken,
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
                authenticatedUsersId = resultUsersId
                sessionRepository.saveLoginSession(
                    accessToken = resultAccessToken,
                    refreshToken = result.refreshToken,
                    deviceIdentifier = deviceRegistration.deviceIdentifier,
                    userId = resultUsersId.toString(),
                    usersId = resultUsersId,
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
            it.copy(signupEmailRequestErrorMessage = null, signupVerificationErrorMessage = null)
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
            val result = request()
            currentCoroutineContext().ensureActive()
            onResult(result)
        }
    }

    private fun launchRequest(
        onFailure: (String) -> Unit = {},
        request: suspend () -> Unit
    ) {
        cancelPendingRequest()
        val job = viewModelScope.launch(start = kotlinx.coroutines.CoroutineStart.LAZY) {
            _uiState.value = AuthUiState(isLoading = true)
            runCatching { request() }
                .onSuccess {
                    currentCoroutineContext().ensureActive()
                    _uiState.value = AuthUiState()
                }
                .onFailure { throwable ->
                    currentCoroutineContext().ensureActive()
                    if (throwable is CancellationException) throw throwable
                    val errorMessage = throwable.message
                        ?: DEFAULT_ERROR_MESSAGE
                    _uiState.value = AuthUiState(
                        errorMessage = errorMessage,
                        errorCode = (throwable as? RemoteRequestException)?.code,
                    )
                    onFailure(errorMessage)
                }
        }
        requestJob = job
        job.start()
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
