package com.example.senior_on.ui.parent.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    val viewModel: ParentEmergencyAlertViewModel = viewModel(
        factory = ParentEmergencyAlertViewModel.factory(
            repository = repository,
            locationRepository = locationRepository,
            deviceRepository = deviceRepository,
            sharingGuard = sharingGuard,
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startCountdown()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    val scope = rememberCoroutineScope()
    var preparing by remember { mutableStateOf(false) }
    suspend fun prepareAlert(sendImmediately: Boolean) {
        if (preparing || viewModel.uiState.value.status == ParentEmergencyAlertStatus.Sending ||
            viewModel.uiState.value.status == ParentEmergencyAlertStatus.Sent) return
        preparing = true
        try {
        val locationShared = try {
            sharingGuard?.refresh()?.locationEnabled == true
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            viewModel.onSharingCheckFailed()
            return
        }
        if (locationShared && !context.hasLocationPermission()) {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        } else if (sendImmediately) {
            viewModel.sendEmergencyAlert()
        } else {
            viewModel.startCountdown()
        }
        } finally {
            preparing = false
        }
    }

    LaunchedEffect(viewModel) {
        if (uiState.status == ParentEmergencyAlertStatus.Idle) prepareAlert(false)
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
            loading = preparing,
            hasContent = false,
            onRetry = { scope.launch { prepareAlert(false) } },
            modifier = modifier,
            title = "긴급알림",
            onBackClick = ::cancelAndGoBack,
        ) {}
    } else {
        ParentEmergencyAlertScreen(
            uiState = uiState,
            onBackClick = ::cancelAndGoBack,
            onSendClick = {
                scope.launch { prepareAlert(true) }
            },
            onCancelClick = ::cancelAndGoBack,
            modifier = modifier,
        )
    }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)
