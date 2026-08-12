package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.ui.onboarding.SplashScreen
import com.example.senior_on.ui.onboarding.login.LoginScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.senior_on.ui.onboarding.social.SocialLoginTokenProvider

@Composable
fun SplashRoute(
    appContainer: AppContainer,
    onSessionLoaded: (AuthSession?) -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)

    LaunchedEffect(Unit) {
        delay(SPLASH_DELAY_MILLIS)
        viewModel.loadSavedSession { session ->
            onSessionLoaded(session)
        }
    }

    SplashScreen()
}

@Composable
fun LoginRoute(
    appContainer: AppContainer,
    selectedMode: AppUserMode,
    onLoginSuccess: (userId: String, keepLoggedIn: Boolean) -> Unit,
    onBackClick: () -> Unit,
    onFindIdClick: () -> Unit,
    onFindPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSocialSignupRequired: () -> Unit,
) {
    val viewModel = onboardingAuthViewModel(appContainer)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LoginScreen(
        selectedMode = selectedMode,
        onLoginRequest = { loginId, password, keepLoggedIn, onResult ->
            viewModel.login(
                loginId = loginId,
                password = password,
                mode = selectedMode,
                keepLoggedIn = keepLoggedIn,
                onResult = onResult,
            )
        },
        onLoginClick = onLoginSuccess,
        onGoToModeSelection = onBackClick,
        onFindIdClick = onFindIdClick,
        onFindPasswordClick = onFindPasswordClick,
        onSignUpClick = onSignUpClick,
        onKakaoLoginRequest = { keepLoggedIn, onResult ->
            scope.launch {
                runCatching {
                    SocialLoginTokenProvider.getKakaoAccessToken(context)
                }.onSuccess { kakaoAccessToken ->
                    viewModel.loginWithKakao(
                        kakaoAccessToken = kakaoAccessToken,
                        mode = selectedMode,
                        keepLoggedIn = keepLoggedIn,
                        onResult = onResult,
                    )
                }.onFailure { onResult(null) }
            }
        },
        onGoogleLoginRequest = { keepLoggedIn, onResult ->
            scope.launch {
                runCatching {
                    SocialLoginTokenProvider.getGoogleFirebaseIdToken(context)
                }.onSuccess { firebaseIdToken ->
                    viewModel.loginWithGoogle(
                        firebaseIdToken = firebaseIdToken,
                        mode = selectedMode,
                        keepLoggedIn = keepLoggedIn,
                        onResult = onResult,
                    )
                }.onFailure { onResult(null) }
            }
        },
        onSocialSignupRequired = onSocialSignupRequired,
    )
}

private const val SPLASH_DELAY_MILLIS = 900L
