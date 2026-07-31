package com.example.senior_on.ui.child.notification.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationHistoryScreen
import com.example.senior_on.ui.child.notification.NotificationMessageUiState

@Composable
internal fun NotificationHistoryRoute(
    category: NotificationCategory,
    messages: List<NotificationMessageUiState>,
    onBackClick: () -> Unit,
    onMessageClick: (NotificationMessageUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    NotificationHistoryScreen(
        category = category,
        messages = messages,
        modifier = modifier,
        onBackClick = onBackClick,
        onMessageClick = onMessageClick,
    )
}
