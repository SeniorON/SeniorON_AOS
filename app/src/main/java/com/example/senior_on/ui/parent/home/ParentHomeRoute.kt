package com.example.senior_on.ui.parent.home

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.R
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.repository.server.HomeServerRepository
import com.example.senior_on.ui.common.homebutton.startDefaultHomeButtonAction
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeButtonUiModel
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeViewModel
import com.example.senior_on.ui.parent.photo.ParentPhotoSourceBottomSheet

@Composable
fun ParentHomeRoute(
    repository: HomeServerRepository,
    refreshRequest: Int = 0,
    onScheduleClick: () -> Unit,
    onChatBuddyClick: () -> Unit,
    onMedicationClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onFamilyPhotosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: ParentHomeViewModel = viewModel(
        factory = ParentHomeViewModel.factory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPhotoSource by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(refreshRequest) {
        if (refreshRequest > 0) {
            viewModel.refresh()
        }
    }

    ParentHomeScreen(
        configuration = uiState.screenConfiguration,
        musicButton = uiState.musicButton,
        buttons = uiState.buttons,
        scheduleUiState = uiState.schedule,
        weatherUiState = uiState.weather,
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        onMusicClick = { openParentHomeButton(context, it) },
        onScheduleClick = onScheduleClick,
        onButtonClick = { button ->
            when (button.type) {
                SeniorHomeButtonType.ChatBuddy -> onChatBuddyClick()
                SeniorHomeButtonType.Medication -> onMedicationClick()
                SeniorHomeButtonType.Schedule -> onScheduleClick()
                SeniorHomeButtonType.Photo -> showPhotoSource = true
                SeniorHomeButtonType.Emergency -> onEmergencyClick()
                else -> openParentHomeButton(context, button)
            }
        },
        modifier = modifier,
    )

    if (showPhotoSource) {
        ParentPhotoSourceBottomSheet(
            onDismiss = { showPhotoSource = false },
            onFamilyPhotosClick = {
                showPhotoSource = false
                onFamilyPhotosClick()
            },
            onGalleryClick = {
                showPhotoSource = false
                openSystemGallery(context)
            },
        )
    }
}

private fun openParentHomeButton(
    context: android.content.Context,
    button: ParentHomeButtonUiModel,
) {
    val packageName = button.packageName?.trim().orEmpty()
    if (button.actionType.equals("APP", ignoreCase = true) && packageName.isNotEmpty()) {
        openAppOrPlayStore(context, packageName)
        return
    }

    when (
        evaluateDefaultAppLaunch(
            actionType = button.actionType,
            actionValue = button.actionValue,
            launch = { actionValue ->
                startDefaultHomeButtonAction(context, actionValue)
            },
        )
    ) {
        DefaultAppLaunchResult.Launched -> return
        DefaultAppLaunchResult.Unavailable -> {
            Toast.makeText(
                context,
                R.string.default_app_unavailable_message,
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        DefaultAppLaunchResult.NotApplicable -> Unit
    }

    button.type?.let { openSeniorHomeButton(context, it) }
}

internal enum class DefaultAppLaunchResult {
    NotApplicable,
    Launched,
    Unavailable,
}

internal fun evaluateDefaultAppLaunch(
    actionType: String?,
    actionValue: String?,
    launch: (String) -> Boolean,
): DefaultAppLaunchResult {
    if (!actionType.equals("DEFAULT", ignoreCase = true) || actionValue.isNullOrBlank()) {
        return DefaultAppLaunchResult.NotApplicable
    }

    return if (launch(actionValue)) {
        DefaultAppLaunchResult.Launched
    } else {
        DefaultAppLaunchResult.Unavailable
    }
}
