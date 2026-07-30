package com.example.senior_on.ui.parent.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.parent.emergency.ParentEmergencyAlertScreen
import com.example.senior_on.ui.parent.emergency.ParentEmergencyAlertSentScreen
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus
import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertUiState

@Composable
fun ParentEmergencyRoute(
    uiState: ParentEmergencyAlertUiState,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.status == ParentEmergencyAlertStatus.Sent) {
        ParentEmergencyAlertSentScreen(
            onBackClick = onBackClick,
            modifier = modifier
        )
    } else {
        ParentEmergencyAlertScreen(
            uiState = uiState,
            onBackClick = onBackClick,
            onSendClick = onSendClick,
            onCancelClick = onCancelClick,
            modifier = modifier
        )
    }
}
