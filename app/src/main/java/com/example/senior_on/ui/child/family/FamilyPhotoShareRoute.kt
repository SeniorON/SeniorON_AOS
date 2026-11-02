package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyPhotoUploadViewModel
import com.example.senior_on.ui.child.family.viewmodel.FamilyPhotoTargetViewModel
import com.example.senior_on.domain.model.server.ServerPhotoTarget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.senior_on.data.local.FamilyPhotoSharePreferences
import kotlinx.coroutines.delay

private const val TooltipAppearanceDelayMillis = 750L
private const val TooltipVisibleDurationMillis = 2_000L

@Composable
fun FamilyPhotoShareRoute(
    photoUri: String,
    uploadSessionId: String,
    onBackClick: () -> Unit,
    onReselectClick: () -> Unit,
    onShareSuccess: () -> Unit,
    seniorId: Long,
    modifier: Modifier = Modifier,
    viewModel: FamilyPhotoUploadViewModel,
    targetViewModel: FamilyPhotoTargetViewModel,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        FamilyPhotoSharePreferences(context)
    }
    val shouldShowTooltip = remember(uploadSessionId) {
        preferences.shouldShowMessageTooltip()
    }
    var message by rememberSaveable(uploadSessionId) { mutableStateOf("") }
    var isTooltipVisible by rememberSaveable(uploadSessionId) { mutableStateOf(false) }
    var hasUserInteracted by rememberSaveable(uploadSessionId) { mutableStateOf(false) }
    var isSuccessDialogVisible by rememberSaveable(uploadSessionId) {
        mutableStateOf(false)
    }
    var isRecipientSheetVisible by rememberSaveable(uploadSessionId) {
        mutableStateOf(false)
    }
    var selectedSeniorIds by rememberSaveable(uploadSessionId) {
        mutableStateOf(emptyList<Long>())
    }
    val uploadUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val targetUiState by targetViewModel.uiState.collectAsStateWithLifecycle()
    val recipients = familyPhotoRecipients(targetUiState.targets)
    val isCurrentUploadSession = uploadUiState.sessionId == uploadSessionId

    LaunchedEffect(uploadSessionId, photoUri) {
        viewModel.startUploadSession(uploadSessionId, photoUri)
    }

    LaunchedEffect(uploadSessionId) {
        targetViewModel.loadTargets(force = true)
    }

    LaunchedEffect(recipients) {
        val availableSeniorIds = recipients
            .map { it.seniorId }
            .toSet()
        selectedSeniorIds = selectedSeniorIds.filter(availableSeniorIds::contains)
    }

    LaunchedEffect(
        uploadSessionId,
        uploadUiState.sessionId,
        uploadUiState.isUploaded,
    ) {
        if (isCurrentUploadSession && uploadUiState.isUploaded) {
            isSuccessDialogVisible = true
        }
    }

    LaunchedEffect(uploadSessionId, shouldShowTooltip) {
        if (!shouldShowTooltip) return@LaunchedEffect

        preferences.markMessageTooltipShown()
        delay(TooltipAppearanceDelayMillis)

        if (hasUserInteracted) return@LaunchedEffect

        isTooltipVisible = true
        delay(TooltipVisibleDurationMillis)
        isTooltipVisible = false
    }

    FamilyPhotoShareScreen(
        photoUri = photoUri,
        message = message,
        onMessageChange = { message = normalizeFamilyPhotoMessage(it) },
        isTooltipVisible = isTooltipVisible,
        onUserInteraction = {
            hasUserInteracted = true
            isTooltipVisible = false
        },
        onBackClick = onBackClick,
        onReselectClick = onReselectClick,
        onShareClick = { isRecipientSheetVisible = true },
        isUploading = isCurrentUploadSession && uploadUiState.isUploading,
        uploadErrorMessage = uploadUiState.errorMessage.takeIf {
            isCurrentUploadSession
        },
        modifier = modifier
    )

    if (isRecipientSheetVisible) {
        FamilyPhotoRecipientBottomSheet(
            seniors = recipients,
            selectedSeniorIds = selectedSeniorIds,
            onSeniorClick = { selectedSeniorId ->
                if (!targetViewModel.uiState.value.canShare(listOf(selectedSeniorId))) {
                    return@FamilyPhotoRecipientBottomSheet
                }
                selectedSeniorIds = togglePhotoRecipientSelection(
                    selectedSeniorIds = selectedSeniorIds,
                    seniorId = selectedSeniorId,
                )
            },
            onDismiss = { isRecipientSheetVisible = false },
            onShareClick = {
                if (!targetViewModel.uiState.value.canShare(selectedSeniorIds)) {
                    return@FamilyPhotoRecipientBottomSheet
                }
                isRecipientSheetVisible = false
                viewModel.uploadPhoto(
                    message = message,
                    seniorId = seniorId,
                    seniorIds = selectedSeniorIds,
                )
            },
            isLoading = targetUiState.isLoading || !targetUiState.hasLoaded,
            errorMessage = targetUiState.errorMessage,
            onRetryClick = {
                targetViewModel.loadTargets(force = true)
            },
        )
    }

    if (isSuccessDialogVisible) {
        FamilyPhotoShareSuccessDialog(
            onConfirmClick = {
                isSuccessDialogVisible = false
                viewModel.consumeUploadSuccess(uploadSessionId)
                onShareSuccess()
            },
        )
    }
}

internal fun familyPhotoRecipients(
    targets: List<ServerPhotoTarget>,
): List<ServerPhotoTarget> = targets.distinctBy(ServerPhotoTarget::seniorId)

internal fun normalizeFamilyPhotoMessage(message: String): String = message.take(30)
