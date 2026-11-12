package com.example.senior_on.ui.parent.emergency

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.server.DeviceRepository
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertViewModel

@Composable
fun ParentEmergencyRoute(
    repository: EventRepository,
    locationRepository: LocationRepository,
    deviceRepository: DeviceRepository,
    sharingGuard: com.example.senior_on.data.repository.impl.ParentSharingGuard?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ParentEmergencyAlertViewModel = viewModel(
        factory = ParentEmergencyAlertViewModel.factory(
            repository = repository,
            locationRepository = locationRepository,
            deviceRepository = deviceRepository,
            sharingGuard = sharingGuard,
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        if (uiState.status == ParentEmergencyAlertStatus.Idle) viewModel.startCountdown()
    }

    fun cancelAndGoBack() {
        viewModel.cancel()
        onBackClick()
    }

    BackHandler(onBack = ::cancelAndGoBack)

    DisposableEffect(viewModel) {
        onDispose(viewModel::reset)
    }

    if (uiState.status == ParentEmergencyAlertStatus.Sent) {
        ParentEmergencyAlertSentScreen(
            onBackClick = ::cancelAndGoBack,
            modifier = modifier,
        )
    } else if (uiState.status == ParentEmergencyAlertStatus.Failed) {
        com.example.senior_on.ui.parent.component.ParentQueryRetryContent(
            error = uiState.errorMessage ?: "긴급알림을 보내지 못했어요.\n다시 시도해 주세요.",
            loading = false,
            hasContent = false,
            onRetry = viewModel::startCountdown,
            modifier = modifier,
            title = "긴급알림",
            onBackClick = ::cancelAndGoBack,
        ) {}
    } else {
        ParentEmergencyAlertScreen(
            uiState = uiState,
            onBackClick = ::cancelAndGoBack,
            onSendClick = viewModel::sendEmergencyAlert,
            onCancelClick = ::cancelAndGoBack,
            modifier = modifier,
        )
    }
}
