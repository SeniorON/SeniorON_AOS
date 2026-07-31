package com.example.senior_on.ui.parent.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertViewModel

@Composable
fun ParentEmergencyRoute(
    repository: EventRepository,
    locationRepository: LocationRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val deviceBattery = remember(context) { context.currentBatteryLevel() }
    val viewModel: ParentEmergencyAlertViewModel = viewModel(
        factory = ParentEmergencyAlertViewModel.factory(
            repository = repository,
            locationRepository = locationRepository,
            deviceBattery = deviceBattery,
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

    LaunchedEffect(viewModel) {
        if (uiState.status == ParentEmergencyAlertStatus.Idle) {
            if (context.hasLocationPermission()) {
                viewModel.startCountdown()
            } else {
                locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
            }
        }
    }

    fun cancelAndGoBack() {
        viewModel.cancel()
        onBackClick()
    }

    if (uiState.status == ParentEmergencyAlertStatus.Sent) {
        ParentEmergencyAlertSentScreen(
            onBackClick = ::cancelAndGoBack,
            modifier = modifier,
        )
    } else {
        ParentEmergencyAlertScreen(
            uiState = uiState,
            onBackClick = ::cancelAndGoBack,
            onSendClick = {
                if (context.hasLocationPermission()) {
                    viewModel.sendEmergencyAlert()
                } else {
                    locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
                }
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

private fun Context.currentBatteryLevel(): Int? {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        ?: return null
    return batteryManager
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .takeIf { it in 0..100 }
}

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)
