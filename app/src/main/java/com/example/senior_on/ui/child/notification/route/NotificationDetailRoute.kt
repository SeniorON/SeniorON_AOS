package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationDetailScreen
import com.example.senior_on.ui.child.notification.NotificationMessageUiState

@Composable
internal fun NotificationDetailRoute(
    category: NotificationCategory,
    message: NotificationMessageUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NotificationDetailScreen(
        category = category,
        message = message,
        modifier = modifier,
        onBackClick = onBackClick,
    )
}
