package com.example.senior_on.ui.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FamilyMemberSettingsRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddFamilyClick: () -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var previouslyHadManagePermission by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.canManageMembers, uiState.isLoading) {
        if (previouslyHadManagePermission && !uiState.canManageMembers) {
            onBackClick()
        }
        if (!uiState.isLoading) {
            previouslyHadManagePermission = uiState.canManageMembers
        }
    }

    FamilyMemberSettingsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddFamilyClick = onAddFamilyClick,
        onChangePrimaryClick = viewModel::changePrimaryMember,
        onDeleteMemberClick = viewModel::deleteMember,
        modifier = modifier
    )
}
