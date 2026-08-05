package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AuthSession
import com.example.senior_on.ui.onboarding.SplashScreen
import com.example.senior_on.ui.onboarding.login.LoginScreen
import kotlinx.coroutines.delay

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
    onLoginSuccess: (userId: String) -> Unit,
    onBackClick: () -> Unit,
    onFindIdClick: () -> Unit,
    onFindPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)

    LoginScreen(
        selectedMode = selectedMode,
        onLoginRequest = { loginId, password, onResult ->
            viewModel.login(
                loginId = loginId,
                password = password,
                mode = selectedMode,
                onResult = onResult,
            )
        },
        onLoginClick = onLoginSuccess,
        onGoToModeSelection = onBackClick,
        onFindIdClick = onFindIdClick,
        onFindPasswordClick = onFindPasswordClick,
        onSignUpClick = onSignUpClick
    )
}

private const val SPLASH_DELAY_MILLIS = 900L
