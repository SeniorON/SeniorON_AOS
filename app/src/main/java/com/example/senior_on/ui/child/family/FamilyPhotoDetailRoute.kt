package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyViewModel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.data.local.FamilyPhotoSaver
import com.example.senior_on.domain.model.family.FamilyImageSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SaveSuccessVisibleDurationMillis = 1_500L

@Composable
fun FamilyPhotoDetailRoute(
    photoId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDownloadClick: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    viewModel: FamilyViewModel,
) {
    val context = LocalContext.current
    val photoSaver = remember(context) { FamilyPhotoSaver(context) }
    val coroutineScope = rememberCoroutineScope()
    val familyUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiState = FamilyPhotoDetailUiState(
        photo = familyUiState.sharedPhotos.firstOrNull { it.id == photoId },
        isLoading = familyUiState.loadingPhotoId == photoId,
        isDeleting = familyUiState.deletingPhotoId == photoId,
        deletedPhotoId = familyUiState.deletedPhotoId,
        errorMessage = familyUiState.photoMutationErrorMessage,
    )
    var isSaving by remember { mutableStateOf(false) }
    var isSaveSuccessVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(photoId) {
        viewModel.ensurePhotoLoaded(photoId)
    }

    LaunchedEffect(uiState.deletedPhotoId, photoId) {
        if (uiState.deletedPhotoId == photoId) onDeleteSuccess()
    }

    LaunchedEffect(isSaveSuccessVisible) {
        if (isSaveSuccessVisible) {
            delay(SaveSuccessVisibleDurationMillis)
            isSaveSuccessVisible = false
        }
    }

    val savePhoto: () -> Unit = {
        if (!isSaving) {
            isSaving = true
            coroutineScope.launch {
                runCatching {
                    val latestImageUrl = viewModel.getLatestPhotoUrlForDownload(photoId)
                    photoSaver.save(
                        imageSource = FamilyImageSource.Remote(latestImageUrl),
                        displayName = "SeniorON_${System.currentTimeMillis()}"
                    )
                }.onSuccess {
                    isSaveSuccessVisible = true
                    onDownloadClick(photoId)
                }
                isSaving = false
            }
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && uiState.photo != null) {
            savePhoto()
        }
    }

    val onDownloadRequest: () -> Unit = {
        if (uiState.photo != null) {
            val needsLegacyPermission =
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED

            if (needsLegacyPermission) {
                storagePermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } else {
                savePhoto()
            }
        }
        Unit
    }

    FamilyPhotoDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        isSaving = isSaving,
        isSaveSuccessVisible = isSaveSuccessVisible,
        isDeleteDialogVisible = isDeleteDialogVisible,
        onDownloadClick = onDownloadRequest,
        onDeleteRequest = { isDeleteDialogVisible = true },
        onDeleteDismiss = { isDeleteDialogVisible = false },
        onDeleteConfirm = {
            isDeleteDialogVisible = false
            viewModel.deletePhoto(photoId)
        },
        onRetryClick = { viewModel.ensurePhotoLoaded(photoId) },
        sharedPhotoImage = { photo ->
            SharedFamilyPhotoImage(
                photo = photo,
                onRemoteImageLoadError = viewModel::refreshPhotoUrlAfterLoadFailure,
            )
        },
        modifier = modifier
    )
}
