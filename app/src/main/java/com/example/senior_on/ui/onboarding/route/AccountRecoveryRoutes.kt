package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.di.AppContainer
import com.example.senior_on.ui.common.account.FindAccountTab
import com.example.senior_on.ui.onboarding.findaccount.FindAccountScreen
import com.example.senior_on.ui.onboarding.findaccount.FindIdResultScreen
import com.example.senior_on.ui.onboarding.findaccount.FindPasswordResetScreen
import com.example.senior_on.ui.onboarding.findaccount.FindPasswordVerifyScreen

@Composable
fun FindAccountRoute(
    appContainer: AppContainer,
    initialTab: FindAccountTab,
    onBackClick: () -> Unit,
    onFindIdResult: () -> Unit,
    onPasswordVerification: () -> Unit
) {
    val viewModel = accountRecoveryViewModel(appContainer)
    DisposableEffect(viewModel) { onDispose { viewModel.cancelPendingRequest() } }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FindAccountScreen(
        requestErrorMessage = uiState.errorMessage,
        onInputChange = viewModel::resetFlow,
        initialTab = initialTab,
        onBackClick = onBackClick,
        onFindIdNextClick = { name, email, onComplete ->
            viewModel.findLoginId(name, email) { result ->
                if (result != null) onFindIdResult()
                onComplete()
            }
        },
        onFindPasswordNextClick = { name, userId, onResult ->
            viewModel.continuePasswordRecovery(
                name = name,
                loginId = userId
            ) { sent ->
                if (sent) {
                    onPasswordVerification()
                }
                onResult(sent)
            }
        }
    )
}

@Composable
fun FindIdResultRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onFindPasswordClick: () -> Unit
) {
    val viewModel = accountRecoveryViewModel(appContainer)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FindIdResultScreen(
        isSuccess = uiState.foundLoginId.isNotBlank(),
        name = uiState.recoveryName,
        userId = uiState.foundLoginId,
        joinDate = uiState.foundJoinDate,
        onBackClick = onBackClick,
        onLoginClick = onLoginClick,
        onFindPasswordClick = onFindPasswordClick
    )
}

@Composable
fun FindPasswordVerifyRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onVerifySuccess: () -> Unit,
    onTabSelected: (FindAccountTab) -> Unit
) {
    val viewModel = accountRecoveryViewModel(appContainer)
    DisposableEffect(viewModel) { onDispose { viewModel.cancelPendingRequest() } }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FindPasswordVerifyScreen(
        onInputChange = viewModel::clearRequestError,
        verificationId = uiState.verificationId,
        isVerified = uiState.passwordResetVerified,
        requestLoginId = uiState.requestLoginId,
        requestErrorMessage = uiState.errorMessage,
        maskedEmail = "계정에 등록된 이메일",
        onBackClick = onBackClick,
        onVerifySuccess = onVerifySuccess,
        onVerifyCode = viewModel::verifyPasswordResetCode,
        onResendCode = viewModel::resendPasswordResetVerificationCode,
        onTabSelected = onTabSelected
    )
}

@Composable
fun FindPasswordResetRoute(
    appContainer: AppContainer,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val viewModel = accountRecoveryViewModel(appContainer)
    DisposableEffect(viewModel) { onDispose { viewModel.cancelPendingRequest() } }

    FindPasswordResetScreen(
        onBackClick = onBackClick,
        onComplete = viewModel::resetPassword,
        onLoginClick = onLoginClick
    )
}
