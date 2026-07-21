package com.example.senior_on.ui.family

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FamilyPhotoGalleryRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onGalleryClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FamilyPhotoGalleryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onGalleryClick = onGalleryClick,
        onCameraClick = onCameraClick,
        onPhotoClick = onPhotoClick,
        onRetryClick = viewModel::loadFamilyOverview,
        modifier = modifier,
    )
}
