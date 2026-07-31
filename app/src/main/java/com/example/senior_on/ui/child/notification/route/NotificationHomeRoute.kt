package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationMessageUiState
import com.example.senior_on.ui.child.notification.NotificationScreen
import com.example.senior_on.ui.child.notification.NotificationScreenUiState

@Composable
internal fun NotificationHomeRoute(
    uiState: NotificationScreenUiState,
    onSectionClick: (NotificationCategory) -> Unit,
    onNotificationClick: (
        NotificationCategory,
        NotificationMessageUiState,
    ) -> Unit,
    onNotificationToggle: (NotificationCategory, Boolean) -> Unit,
    onDetectionTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NotificationScreen(
        uiState = uiState,
        modifier = modifier,
        onSectionClick = onSectionClick,
        onNotificationClick = onNotificationClick,
        onNotificationToggle = onNotificationToggle,
        onDetectionTimeClick = onDetectionTimeClick,
    )
}
