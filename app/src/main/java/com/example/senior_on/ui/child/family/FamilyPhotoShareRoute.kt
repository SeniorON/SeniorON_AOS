package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.child.family.viewmodel.FamilyPhotoUploadViewModel

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
    modifier: Modifier = Modifier,
    viewModel: FamilyPhotoUploadViewModel,
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
    val uploadUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isCurrentUploadSession = uploadUiState.sessionId == uploadSessionId

    LaunchedEffect(uploadSessionId, photoUri) {
        viewModel.startUploadSession(uploadSessionId, photoUri)
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
        onShareClick = { viewModel.uploadPhoto(message) },
        isUploading = isCurrentUploadSession && uploadUiState.isUploading,
        uploadErrorMessage = uploadUiState.errorMessage.takeIf {
            isCurrentUploadSession
        },
        modifier = modifier
    )

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

internal fun normalizeFamilyPhotoMessage(message: String): String = message.take(30)
