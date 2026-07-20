package com.example.senior_on.ui.family

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
    val shouldShowTooltip = remember(photoUri) {
        preferences.shouldShowMessageTooltip()
    }
    var message by rememberSaveable(photoUri) { mutableStateOf("") }
    var isTooltipVisible by rememberSaveable(photoUri) { mutableStateOf(false) }
    var hasUserInteracted by rememberSaveable(photoUri) { mutableStateOf(false) }
    var isSuccessDialogVisible by rememberSaveable(photoUri) { mutableStateOf(false) }
    var isSelectionInitialized by remember(photoUri) { mutableStateOf(false) }
    val uploadUiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(photoUri) {
        viewModel.selectPhoto(photoUri)
        isSelectionInitialized = true
    }

    LaunchedEffect(isSelectionInitialized, uploadUiState.isUploaded) {
        if (isSelectionInitialized && uploadUiState.isUploaded) {
            isSuccessDialogVisible = true
        }
    }

    LaunchedEffect(photoUri, shouldShowTooltip) {
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
        isUploading = uploadUiState.isUploading,
        uploadErrorMessage = uploadUiState.errorMessage,
        modifier = modifier
    )

    if (isSuccessDialogVisible) {
        FamilyPhotoShareSuccessDialog(
            onConfirmClick = {
                isSuccessDialogVisible = false
                onShareSuccess()
            },
        )
    }
}

internal fun normalizeFamilyPhotoMessage(message: String): String = message.take(30)
