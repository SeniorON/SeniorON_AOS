package com.example.senior_on.ui.child.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.child.family.viewmodel.FamilyInvitationViewModel

@Composable
fun FamilyInvitationRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onKakaoShareClick: () -> Unit = {},
    onMessageShareClick: () -> Unit = {},
    repository: FamilyServerRepository,
    viewModelKey: String,
) {
    val viewModel: FamilyInvitationViewModel = viewModel(
        key = viewModelKey,
        factory = FamilyInvitationViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.refresh()
    }

    FamilyInvitationScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onKakaoShareClick = onKakaoShareClick,
        onMessageShareClick = onMessageShareClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}
