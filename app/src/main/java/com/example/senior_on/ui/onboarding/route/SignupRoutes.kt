package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.ui.onboarding.signup.SignupAccountInfoScreen
import com.example.senior_on.ui.onboarding.signup.SignupEmailVerificationScreen
import com.example.senior_on.ui.onboarding.signup.SignupModeGuideScreen
import com.example.senior_on.ui.onboarding.signup.SignupNameBirthScreen
import com.example.senior_on.ui.onboarding.signup.SignupScreen
import com.example.senior_on.ui.onboarding.signup.SignupTermsAgreementScreen

@Composable
fun SignupEntryRoute(
    onBackClick: () -> Unit,
    onKakaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onEmailClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    SignupScreen(
        onBackClick = onBackClick,
        onKakaoClick = onKakaoClick,
        onGoogleClick = onGoogleClick,
        onEmailClick = onEmailClick,
        onLoginClick = onLoginClick
    )
}

@Composable
fun SignupModeGuideRoute(
    onBackClick: () -> Unit,
    onReselectClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    SignupModeGuideScreen(
        onBackClick = onBackClick,
        onReselectClick = onReselectClick,
        onContinueClick = onContinueClick
    )
}

@Composable
fun SignupNameBirthRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)

    SignupNameBirthScreen(
        onBackClick = onBackClick,
        onNextClick = { name, birth ->
            viewModel.saveNameAndBirth(name, birth)
            onNextClick()
        }
    )
}

@Composable
fun SignupEmailVerificationRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SignupEmailVerificationScreen(
        onBackClick = onBackClick,
        onNextClick = { email ->
            viewModel.saveVerifiedEmail(email)
            onNextClick()
        },
        onSendVerificationCode =
            viewModel::sendSignupEmailVerificationCode,
        onVerifyCode =
            viewModel::verifySignupEmailVerificationCode,
        emailRequestErrorMessage =
            uiState.signupEmailRequestErrorMessage,
        onEmailChange =
            viewModel::clearSignupEmailRequestError,
    )
}

@Composable
fun SignupAccountInfoRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)

    SignupAccountInfoScreen(
        onBackClick = onBackClick,
        onNextClick = { loginId, password, passwordCheck ->
            viewModel.saveAccountInfo(
                loginId = loginId,
                password = password,
                passwordCheck = passwordCheck
            )
            onNextClick()
        },
        onCheckLoginId = viewModel::checkLoginId
    )
}

@Composable
fun SignupTermsRoute(
    appContainer: AppContainer,
    selectedMode: AppUserMode,
    onBackClick: () -> Unit,
    onSignupSuccess: (userId: String) -> Unit
) {
    val viewModel = onboardingAuthViewModel(appContainer)

    SignupTermsAgreementScreen(
        onBackClick = onBackClick,
        onCompleteClick = { agreements, onResult ->
            viewModel.completeSignup(
                mode = selectedMode,
                agreements = agreements
            ) { loginResult ->
                if (loginResult != null) {
                    onSignupSuccess(loginResult.loginId)
                }
                onResult(loginResult != null)
            }
        }
    )
}
