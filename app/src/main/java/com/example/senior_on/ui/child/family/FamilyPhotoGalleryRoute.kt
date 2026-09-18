package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FamilyPhotoGalleryRoute(
    seniorId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onGalleryClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onPhotoClick: (String) -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, seniorId) {
        viewModel.loadPhotoGallery(seniorId)
    }

    FamilyPhotoGalleryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onGalleryClick = onGalleryClick,
        onCameraClick = onCameraClick,
        onPhotoClick = onPhotoClick,
        onRetryClick = {
            viewModel.loadFamilyOverview(seniorId)
            viewModel.loadPhotoGallery(seniorId = seniorId, force = true)
        },
        onLoadMore = { viewModel.loadMorePhotos(seniorId) },
        sharedPhotoImage = { photo ->
            SharedFamilyPhotoImage(
                photo = photo,
                onRemoteImageLoadError = viewModel::refreshPhotoUrlAfterLoadFailure,
            )
        },
        modifier = modifier,
    )
}
