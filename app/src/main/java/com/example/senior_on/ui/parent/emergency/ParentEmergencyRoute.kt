package com.example.senior_on.ui.parent.emergency

import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.domain.repository.server.EventRepository
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertViewModel

@Composable
fun ParentEmergencyRoute(
    repository: EventRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val deviceBattery = remember(context) { context.currentBatteryLevel() }
    val viewModel: ParentEmergencyAlertViewModel = viewModel(
        factory = ParentEmergencyAlertViewModel.factory(
            repository = repository,
            latitude = SEONGDONG_DISTRICT_OFFICE_LATITUDE,
            longitude = SEONGDONG_DISTRICT_OFFICE_LONGITUDE,
            deviceBattery = deviceBattery,
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.status == ParentEmergencyAlertStatus.Idle) {
            viewModel.startCountdown()
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
            onSendClick = viewModel::sendEmergencyAlert,
            onCancelClick = ::cancelAndGoBack,
            modifier = modifier,
        )
    }
}

private fun Context.currentBatteryLevel(): Int? {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        ?: return null
    return batteryManager
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        .takeIf { it in 0..100 }
}

private const val SEONGDONG_DISTRICT_OFFICE_LATITUDE = 37.56342697
private const val SEONGDONG_DISTRICT_OFFICE_LONGITUDE = 127.03693390
