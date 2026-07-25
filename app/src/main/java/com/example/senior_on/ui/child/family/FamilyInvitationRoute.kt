package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FamilyInvitationRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onKakaoShareClick: () -> Unit = {},
    onMessageShareClick: () -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FamilyInvitationScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onKakaoShareClick = onKakaoShareClick,
        onMessageShareClick = onMessageShareClick,
        modifier = modifier
    )
}
