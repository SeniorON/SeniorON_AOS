package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val lifecycleOwner = LocalLifecycleOwner.current
    var skipInitialResume by remember(viewModel, seniorId, lifecycleOwner) {
        mutableStateOf(!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    LaunchedEffect(viewModel, seniorId) {
        viewModel.loadPhotoGallery(seniorId)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (skipInitialResume) {
            skipInitialResume = false
        } else {
            viewModel.loadPhotoGallery(seniorId = seniorId, force = true)
        }
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
        isRefreshing = uiState.isPhotoRefreshing,
        onRefresh = { viewModel.refreshPhotoGallery(seniorId) },
        sharedPhotoImage = { photo ->
            SharedFamilyPhotoImage(
                photo = photo,
                onRemoteImageLoadError = viewModel::refreshPhotoUrlAfterLoadFailure,
            )
        },
        modifier = modifier,
    )
}
