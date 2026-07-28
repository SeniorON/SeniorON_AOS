package com.example.senior_on.ui.child.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.senior_on.ui.child.notification.mock.MockNotificationScenario
import com.example.senior_on.ui.child.notification.mock.MockNotificationUiStateFactory

@Composable
fun NotificationRoute(
    scenario: MockNotificationScenario,
    modifier: Modifier = Modifier,
) {
    var showDetectionTimeSetting by rememberSaveable { mutableStateOf(false) }
    var historyCategory by rememberSaveable {
        mutableStateOf<NotificationCategory?>(null)
    }
    var notificationDetail by remember {
        mutableStateOf<Pair<NotificationCategory, NotificationMessageUiState>?>(null)
    }

    notificationDetail?.let { (category, message) ->
        NotificationDetailScreen(
            category = category,
            message = message,
            modifier = modifier,
            onBackClick = { notificationDetail = null },
        )
        return
    }

    historyCategory?.let { category ->
        NotificationHistoryScreen(
            category = category,
            messages = MockNotificationUiStateFactory.getNotificationHistory(category),
            modifier = modifier,
            onBackClick = { historyCategory = null },
            onMessageClick = { message ->
                notificationDetail = category to message
            },
        )
        return
    }

    if (showDetectionTimeSetting) {
        NotificationDetectionTimeSettingScreen(
            modifier = modifier,
            onBackClick = { showDetectionTimeSetting = false },
            onSaveClick = { showDetectionTimeSetting = false },
        )
        return
    }

    NotificationScreen(
        uiState = MockNotificationUiStateFactory.getNotificationState(scenario),
        modifier = modifier,
        onSectionClick = { category -> historyCategory = category },
        onNotificationClick = { category, message ->
            notificationDetail = category to message
        },
        onDetectionTimeClick = { showDetectionTimeSetting = true },
    )
}
