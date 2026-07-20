package com.example.senior_on.ui.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FamilyTabRoute(
    modifier: Modifier = Modifier,
    onMemberSettingsClick: () -> Unit = {},
    onAddFamilyClick: () -> Unit = {},
    onInviteFamilyClick: () -> Unit = {},
    onMorePhotosClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isPhotoSourceSheetVisible by rememberSaveable { mutableStateOf(false) }

    FamilyTabScreen(
        modifier = modifier,
        uiState = uiState,
        onMemberSettingsClick = onMemberSettingsClick,
        onAddFamilyClick = onAddFamilyClick,
        onInviteFamilyClick = onInviteFamilyClick,
        onMorePhotosClick = onMorePhotosClick,
        onUploadPhotoClick = { isPhotoSourceSheetVisible = true },
        onPhotoClick = onPhotoClick,
        onRetryClick = viewModel::loadFamilyOverview,
    )

    if (isPhotoSourceSheetVisible) {
        FamilyPhotoSourceBottomSheet(
            onDismiss = { isPhotoSourceSheetVisible = false },
            onGalleryClick = {
                isPhotoSourceSheetVisible = false
                onGalleryClick()
            },
            onCameraClick = {
                isPhotoSourceSheetVisible = false
                onCameraClick()
            }
        )
    }
}
