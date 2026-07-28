package com.example.senior_on.ui.onboarding.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.di.AppContainer
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.ui.onboarding.ModeSelectionScreen
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeCreatedScreen
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeInputScreen
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeOption
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeScreen
import com.example.senior_on.ui.onboarding.viewmodel.FamilyConnectionViewModel

@Composable
fun ModeSelectionRoute(
    onChildClick: () -> Unit,
    onSeniorClick: () -> Unit
) {
    ModeSelectionScreen(onChildClick = onChildClick, onSeniorClick = onSeniorClick)
}

@Composable
fun FamilyShareCodeRoute(
    onBackClick: () -> Unit,
    onNextClick: (FamilyShareCodeOption) -> Unit
) {
    FamilyShareCodeScreen(onBackClick = onBackClick, onNextClick = onNextClick)
}

@Composable
fun FamilyShareCodeInputRoute(
    appContainer: AppContainer,
    userId: String,
    onBackClick: () -> Unit,
    onJoinSuccess: (FamilyCodeInfo) -> Unit
) {
    val viewModel: FamilyConnectionViewModel = viewModel(
        key = "family-connection-$userId",
        factory = FamilyConnectionViewModel.Factory(
            appContainer.familyServerRepository
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FamilyShareCodeInputScreen(
        onBackClick = {
            viewModel.clearError()
            onBackClick()
        },
        onLoginClick = { code -> viewModel.joinFamily(code, onJoinSuccess) },
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onFamilyShareCodeChange = viewModel::clearError
    )
}

@Composable
fun FamilyShareCodeCreatedRoute(
    appContainer: AppContainer,
    userId: String,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val viewModel: FamilyConnectionViewModel = viewModel(
        key = "family-code-create-$userId",
        factory = FamilyConnectionViewModel.Factory(
            appContainer.familyServerRepository
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.createFamilyCode()
    }

    FamilyShareCodeCreatedScreen(
        onBackClick = onBackClick,
        onNextClick = onNextClick,
        familyShareCode = uiState.familyCode.orEmpty(),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onRetryClick = viewModel::retryCreateFamilyCode,
    )
}
