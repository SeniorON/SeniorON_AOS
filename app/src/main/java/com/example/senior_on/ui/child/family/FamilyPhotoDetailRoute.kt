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
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.data.local.FamilyPhotoSaver
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyPhotoReportReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SaveSuccessVisibleDurationMillis = 1_500L

@Composable
fun FamilyPhotoDetailRoute(
    seniorId: Long,
    photoId: String,
    currentUserId: Long?,
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
    val viewerUserId = familyUiState.members.firstOrNull { it.isCurrentUser }
        ?.id?.toLongOrNull() ?: currentUserId
    var displayedPhotoId by remember(photoId) { mutableStateOf(photoId) }
    val displayedPhoto = familyUiState.sharedPhotos.firstOrNull {
        it.id == displayedPhotoId
    }
    val uiState = FamilyPhotoDetailUiState(
        photo = displayedPhoto,
        isLoading = familyUiState.loadingPhotoId == displayedPhotoId,
        isDeleting = familyUiState.deletingPhotoId == displayedPhotoId,
        deletedPhotoId = familyUiState.deletedPhotoId,
        errorMessage = familyUiState.photoMutationErrorMessage,
    )
    var isSaving by remember { mutableStateOf(false) }
    var isSaveSuccessVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }
    var isReportTargetSheetVisible by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<FamilyPhotoReportTarget?>(null) }
    var selectedReportReason by remember { mutableStateOf<FamilyPhotoReportReason?>(null) }
    var reportDetail by remember { mutableStateOf("") }
    var hideReportedContent by remember { mutableStateOf(false) }
    var isReportSuccessVisible by remember { mutableStateOf(false) }
    var completedReportTarget by remember { mutableStateOf<FamilyPhotoReportTarget?>(null) }
    var completedReportHide by remember { mutableStateOf(false) }
    var completedReportUploaderId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(seniorId, photoId) {
        viewModel.ensurePhotoLoaded(photoId)
        viewModel.loadPhotoGallery(seniorId)
    }

    LaunchedEffect(uiState.deletedPhotoId, displayedPhotoId) {
        if (uiState.deletedPhotoId == displayedPhotoId) onDeleteSuccess()
    }

    LaunchedEffect(familyUiState.reportedPhotoId, displayedPhotoId) {
        if (familyUiState.reportedPhotoId == displayedPhotoId) {
            completedReportTarget = reportTarget
            completedReportHide = hideReportedContent
            completedReportUploaderId = displayedPhoto?.authorId
            reportTarget = null
            selectedReportReason = null
            reportDetail = ""
            isReportSuccessVisible = true
        }
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
                    val latestImageUrl = viewModel.getLatestPhotoUrlForDownload(displayedPhotoId)
                    photoSaver.save(
                        imageSource = FamilyImageSource.Remote(latestImageUrl),
                        displayName = "SeniorON_${System.currentTimeMillis()}"
                    )
                }.onSuccess {
                    isSaveSuccessVisible = true
                    onDownloadClick(displayedPhotoId)
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
        currentUserId = viewerUserId,
        photos = familyUiState.sharedPhotos,
        initialPhotoId = photoId,
        onDisplayedPhotoChange = { photo -> displayedPhotoId = photo.id },
        onLoadMore = { viewModel.loadMorePhotos(seniorId) },
        onBackClick = onBackClick,
        isSaving = isSaving,
        isReporting = familyUiState.reportingPhotoId == displayedPhotoId,
        isSaveSuccessVisible = isSaveSuccessVisible,
        isDeleteDialogVisible = isDeleteDialogVisible,
        onDownloadClick = onDownloadRequest,
        onDeleteRequest = { isDeleteDialogVisible = true },
        onReportRequest = {
            viewModel.clearPhotoReportFeedback()
            isReportTargetSheetVisible = true
        },
        onDeleteDismiss = { isDeleteDialogVisible = false },
        onDeleteConfirm = {
            isDeleteDialogVisible = false
            viewModel.deletePhoto(displayedPhotoId)
        },
        onRetryClick = { viewModel.ensurePhotoLoaded(photoId) },
        sharedPhotoImage = { photo ->
            SharedFamilyPhotoImage(
                photo = photo,
                preferThumbnail = false,
                contentScale = ContentScale.Fit,
                onRemoteImageLoadError = viewModel::refreshPhotoUrlAfterLoadFailure,
            )
        },
        modifier = modifier
    )

    val photo = uiState.photo
    if (isReportTargetSheetVisible && photo != null) {
        FamilyPhotoReportTargetBottomSheet(
            authorName = photo.authorName,
            onDismiss = { isReportTargetSheetVisible = false },
            onTargetSelected = { target ->
                isReportTargetSheetVisible = false
                selectedReportReason = null
                reportDetail = ""
                hideReportedContent = false
                reportTarget = target
            },
        )
    }

    val currentReportTarget = reportTarget
    if (currentReportTarget != null && photo != null) {
        FamilyPhotoReportReasonDialog(
            target = currentReportTarget,
            selectedReason = selectedReportReason,
            detail = reportDetail,
            hide = hideReportedContent,
            isSubmitting = familyUiState.reportingPhotoId == photo.id,
            errorMessage = familyUiState.photoReportErrorMessage,
            onReasonSelected = {
                viewModel.clearPhotoReportFeedback()
                selectedReportReason = it
            },
            onDetailChanged = { reportDetail = it.take(MaxReportDetailLength) },
            onHideChanged = { hideReportedContent = it },
            onDismiss = {
                viewModel.clearPhotoReportFeedback()
                reportTarget = null
                selectedReportReason = null
                reportDetail = ""
                hideReportedContent = false
            },
            onSubmit = {
                selectedReportReason?.let { reason ->
                    when (currentReportTarget) {
                        FamilyPhotoReportTarget.Post -> viewModel.reportPhoto(
                            photoId = photo.id,
                            reason = reason,
                            detail = reportDetail,
                            hide = hideReportedContent,
                        )
                        FamilyPhotoReportTarget.Author -> viewModel.reportUploader(
                            photoId = photo.id,
                            reason = reason,
                            detail = reportDetail,
                            hide = hideReportedContent,
                        )
                    }
                }
            },
        )
    }

    if (isReportSuccessVisible) {
        FamilyPhotoReportSuccessDialog(
            onConfirmClick = {
                isReportSuccessVisible = false
                val hidesCurrentPhoto = completedReportHide &&
                    (completedReportTarget == FamilyPhotoReportTarget.Post ||
                        completedReportUploaderId == null)
                val hiddenUploaderId = completedReportUploaderId.takeIf {
                    completedReportHide && completedReportTarget == FamilyPhotoReportTarget.Author
                }
                viewModel.clearPhotoReportFeedback(
                    removeReportedPhoto = hidesCurrentPhoto,
                    hiddenUploaderId = hiddenUploaderId,
                )
                completedReportTarget = null
                completedReportHide = false
                completedReportUploaderId = null
                onBackClick()
            },
        )
    }
}

private const val MaxReportDetailLength = 200
