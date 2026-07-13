package com.example.senior_on.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
fun NotificationScreen(
    sections: List<NotificationSectionUiState>,
    modifier: Modifier = Modifier,
    onTabClick: (SeniorOnMainTab) -> Unit = {},
    onSectionClick: (NotificationCategory) -> Unit = {}
) {
    val messageCount = sections.sumOf { it.messages.size }
    val severity = when {
        sections.any { section ->
            section.messages.any { it.severity == NotificationSeverity.Danger }
        } -> NotificationSeverity.Danger
        messageCount > 0 -> NotificationSeverity.Normal
        else -> NotificationSeverity.Empty
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
            .statusBarsPadding()
    ) {
        NotificationTopBar(
            severity = severity,
            count = messageCount
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            sections.forEach { section ->
                NotificationSectionCard(
                    section = section,
                    showDetailArrow = severity != NotificationSeverity.Empty,
                    onClick = { onSectionClick(section.category) }
                )
            }

            if (severity == NotificationSeverity.Empty) {
                Spacer(modifier = Modifier.height(38.dp))
                NotificationEmptyState()
            }
        }

        SeniorOnBottomNavigation(
            selectedItem = SeniorOnMainTab.Notification,
            onItemClick = onTabClick
        )
    }
}

internal fun emptyNotificationSections(): List<NotificationSectionUiState> = listOf(
    NotificationSectionUiState(
        category = NotificationCategory.Sos,
        title = "SOS 알림",
        description = "부모님의 긴급 도움 요청을 알려드려요.",
        enabled = false
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Inactivity,
        title = "무활동 감지",
        description = "부모님의 활동 상태를 확인할 수 있어요.",
        enabled = false
    ),
    NotificationSectionUiState(
        category = NotificationCategory.RiskLink,
        title = "위험 링크 감지",
        description = "의심되는 링크 접근을 알려드려요.",
        enabled = false
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Outing,
        title = "외출·귀가 알림",
        description = "부모님의 외출 및 귀가를 확인할 수 있어요.",
        enabled = false
    )
)

private fun normalNotificationSections(): List<NotificationSectionUiState> =
    emptyNotificationSections().map { section ->
        section.copy(
            enabled = true,
            messages = emptyList()
        )
    }

private fun dangerNotificationSections(): List<NotificationSectionUiState> = listOf(
    NotificationSectionUiState(
        category = NotificationCategory.Sos,
        title = "SOS 알림",
        description = "부모님의 긴급 도움 요청을 알려드려요.",
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오늘 오전 10:00",
                title = "어머니 · 경기도 하남시 창우동",
                severity = NotificationSeverity.Danger
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Inactivity,
        title = "무활동 감지",
        description = "부모님의 활동 상태를 확인할 수 있어요.",
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오전 10:00부터",
                title = "4시간 미사용 감지됨",
                severity = NotificationSeverity.Danger
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.RiskLink,
        title = "위험 링크 감지",
        description = "의심되는 링크 접근을 알려드려요.",
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오전 10:00",
                title = "http://fake-bank.xyz",
                severity = NotificationSeverity.Danger
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Outing,
        title = "외출·귀가 알림",
        description = "부모님의 외출 및 귀가를 확인할 수 있어요.",
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오전 10:00",
                title = "외출 나가셨어요",
                severity = NotificationSeverity.Empty,
                tintBackground = false
            )
        )
    )
)

@Preview(
    name = "Notification Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationEmptyPreview() {
    SENIOR_ONTheme {
        NotificationScreen(
            sections = emptyNotificationSections()
        )
    }
}

@Preview(
    name = "Notification Normal",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationNormalPreview() {
    SENIOR_ONTheme {
        NotificationScreen(
            sections = normalNotificationSections()
        )
    }
}

@Preview(
    name = "Notification Danger",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationDangerPreview() {
    SENIOR_ONTheme {
        NotificationScreen(
            sections = dangerNotificationSections()
        )
    }
}
