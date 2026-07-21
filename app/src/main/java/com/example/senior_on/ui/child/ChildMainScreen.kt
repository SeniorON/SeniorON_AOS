package com.example.senior_on.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.data.notification.MockNotificationRepository
import com.example.senior_on.data.notification.MockNotificationScenario
import com.example.senior_on.ui.notification.NotificationDetectionTimeSettingScreen
import com.example.senior_on.ui.notification.NotificationCategory
import com.example.senior_on.ui.notification.NotificationDetailScreen
import com.example.senior_on.ui.notification.NotificationHistoryScreen
import com.example.senior_on.ui.notification.NotificationMessageUiState
import com.example.senior_on.ui.notification.NotificationScreen
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun ChildMainScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(ChildMainTab.Screen) }
    var showDetectionTimeSetting by rememberSaveable { mutableStateOf(false) }
    var historyCategory by rememberSaveable { mutableStateOf<NotificationCategory?>(null) }
    var notificationDetail by remember {
        mutableStateOf<Pair<NotificationCategory, NotificationMessageUiState>?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
    ) {
        ChildMainTabContent(
            selectedTab = selectedTab,
            showDetectionTimeSetting = showDetectionTimeSetting,
            historyCategory = historyCategory,
            notificationDetail = notificationDetail,
            onOpenDetectionTimeSetting = { showDetectionTimeSetting = true },
            onCloseDetectionTimeSetting = { showDetectionTimeSetting = false },
            onOpenHistory = { category -> historyCategory = category },
            onCloseHistory = { historyCategory = null },
            onOpenNotificationDetail = { category, message ->
                notificationDetail = category to message
            },
            onCloseNotificationDetail = { notificationDetail = null },
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        )

        ChildBottomNavigation(
            selectedTab = selectedTab,
            onTabClick = { tab ->
                selectedTab = tab
                if (tab != ChildMainTab.Notification) {
                    showDetectionTimeSetting = false
                    historyCategory = null
                    notificationDetail = null
                }
            }
        )
    }
}

@Composable
private fun ChildMainTabContent(
    selectedTab: ChildMainTab,
    showDetectionTimeSetting: Boolean,
    historyCategory: NotificationCategory?,
    notificationDetail: Pair<NotificationCategory, NotificationMessageUiState>?,
    onOpenDetectionTimeSetting: () -> Unit,
    onCloseDetectionTimeSetting: () -> Unit,
    onOpenHistory: (NotificationCategory) -> Unit,
    onCloseHistory: () -> Unit,
    onOpenNotificationDetail: (NotificationCategory, NotificationMessageUiState) -> Unit,
    onCloseNotificationDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedTab == ChildMainTab.Notification) {
        notificationDetail?.let { (category, message) ->
            NotificationDetailScreen(
                category = category,
                message = message,
                modifier = modifier,
                onBackClick = onCloseNotificationDetail
            )
            return
        }

        historyCategory?.let { category ->
            NotificationHistoryScreen(
                category = category,
                messages = MockNotificationRepository.getNotificationHistory(category),
                modifier = modifier,
                onBackClick = onCloseHistory,
                onMessageClick = { message ->
                    onOpenNotificationDetail(category, message)
                }
            )
            return
        }

        if (showDetectionTimeSetting) {
            NotificationDetectionTimeSettingScreen(
                modifier = modifier,
                onBackClick = onCloseDetectionTimeSetting,
                onSaveClick = { onCloseDetectionTimeSetting() }
            )
            return
        }

        val notificationState = MockNotificationRepository.getNotificationState(
            scenario = MockNotificationScenario.RecentAlarms
        )

        NotificationScreen(
            uiState = notificationState,
            modifier = modifier,
            onSectionClick = onOpenHistory,
            onNotificationClick = onOpenNotificationDetail,
            onDetectionTimeClick = onOpenDetectionTimeSetting
        )
        return
    }

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = selectedTab.iconResId),
                contentDescription = null,
                tint = SeniorOnColors.Primary600
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${selectedTab.label} 화면 준비 중이에요",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "자녀 메인 화면 흐름과 바텀네비 연결을 먼저 맞췄어요.",
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(
    name = "Child Main",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ChildMainScreenPreview() {
    SENIOR_ONTheme {
        ChildMainScreen()
    }
}
