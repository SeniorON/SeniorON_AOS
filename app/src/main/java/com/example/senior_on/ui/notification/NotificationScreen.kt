package com.example.senior_on.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors

@Composable
fun NotificationScreen(
    sections: List<NotificationSectionUiState>,
    modifier: Modifier = Modifier,
    onSectionClick: (NotificationCategory) -> Unit = {},
    onNotificationToggle: (NotificationCategory, Boolean) -> Unit = { _, _ -> },
    onDetectionTimeClick: () -> Unit = {}
) {
    NotificationScreen(
        uiState = NotificationScreenUiState(
            sections = sections,
            footerPanel = if (sections.none { it.enabled }) {
                NotificationFooterPanelUiState(tone = NotificationFooterTone.Recommendation)
            } else {
                null
            }
        ),
        modifier = modifier,
        onSectionClick = onSectionClick,
        onNotificationToggle = onNotificationToggle,
        onDetectionTimeClick = onDetectionTimeClick
    )
}

@Composable
fun NotificationScreen(
    uiState: NotificationScreenUiState,
    modifier: Modifier = Modifier,
    onSectionClick: (NotificationCategory) -> Unit = {},
    onNotificationToggle: (NotificationCategory, Boolean) -> Unit = { _, _ -> },
    onDetectionTimeClick: () -> Unit = {}
) {
    var sections by remember(uiState.sections) {
        mutableStateOf(uiState.sections)
    }
    var showHomeAddressMissingDialog by remember { mutableStateOf(false) }
    var showParentPhoneInternetRequiredDialog by remember { mutableStateOf(false) }
    val enabledSections = sections.filter { section -> section.enabled }
    val recentMessages = enabledSections.flatMap { section ->
        section.messages.filter { message -> message.isRecentAlarm() }
    }
    val severity = when {
        recentMessages.any { message ->
            message.severity == NotificationSeverity.Danger
        } -> NotificationSeverity.Danger

        enabledSections.isNotEmpty() -> NotificationSeverity.Normal
        else -> NotificationSeverity.Empty
    }
    val notificationCount = when (severity) {
        NotificationSeverity.Danger -> recentMessages.size
        NotificationSeverity.Normal -> enabledSections.size
        NotificationSeverity.Empty -> 0
    }
    val footerPanel = when {
        uiState.footerPanel?.tone == NotificationFooterTone.Warning -> uiState.footerPanel
        sections.none { section -> section.enabled } -> {
            NotificationFooterPanelUiState(tone = NotificationFooterTone.Recommendation)
        }

        else -> null
    }
    var latestFooterPanel by remember {
        mutableStateOf(footerPanel)
    }
    if (footerPanel != null) {
        latestFooterPanel = footerPanel
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
    ) {
        NotificationTopBar(
            severity = severity,
            count = notificationCount
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            sections.forEachIndexed { index, section ->
                NotificationSectionCard(
                    section = section,
                    showDetailArrow = section.enabled,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textMuted = footerPanel?.tone == NotificationFooterTone.Warning,
                    onClick = { onSectionClick(section.category) },
                    onToggleClick = {
                        when {
                            !uiState.isParentPhoneInternetConnected -> {
                                showParentPhoneInternetRequiredDialog = true
                            }

                            section.category == NotificationCategory.Outing &&
                                !uiState.hasHomeAddress -> {
                                showHomeAddressMissingDialog = true
                            }

                            else -> {
                                val toggledEnabled = !section.enabled
                                sections = sections.map { item ->
                                    if (item.category == section.category) {
                                        item.copy(enabled = toggledEnabled)
                                    } else {
                                        item
                                    }
                                }
                                onNotificationToggle(section.category, toggledEnabled)
                            }
                        }
                    },
                    onDetectionTimeClick = onDetectionTimeClick
                )

                val nextSection = sections.getOrNull(index + 1)
                when {
                    section.category == NotificationCategory.Sos &&
                        nextSection?.category == NotificationCategory.Inactivity -> {
                        NotificationSectionDivider()
                    }

                    section.category == NotificationCategory.Inactivity &&
                        nextSection?.category == NotificationCategory.RiskLink -> {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    section.category == NotificationCategory.RiskLink &&
                        nextSection?.category == NotificationCategory.Outing -> {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    nextSection != null -> {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = footerPanel != null,
                enter = fadeIn(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(180))
            ) {
                latestFooterPanel?.let { panel ->
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        NotificationFooterPanel(
                            uiState = panel,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .height(200.dp)
                        )
                    }
                }
            }
        }
    }

    if (showHomeAddressMissingDialog) {
        HomeAddressMissingDialog(
            onConfirmClick = { showHomeAddressMissingDialog = false }
        )
    }

    if (showParentPhoneInternetRequiredDialog) {
        ParentPhoneInternetRequiredDialog(
            onConfirmClick = { showParentPhoneInternetRequiredDialog = false }
        )
    }
}

private fun NotificationMessageUiState.isRecentAlarm(
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    val occurredAt = occurredAtMillis ?: return true
    val ageMillis = nowMillis - occurredAt
    return ageMillis in 0..RECENT_ALARM_WINDOW_MILLIS
}

private const val RECENT_ALARM_WINDOW_MILLIS = 48L * 60L * 60L * 1000L

@Composable
private fun NotificationSectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(SeniorOnColors.Gray100)
    )
}

internal fun emptyNotificationSections(): List<NotificationSectionUiState> =
    NotificationCategory.entries.map { category ->
        NotificationSectionUiState(
            category = category,
            enabled = false
        )
    }

private fun normalNotificationSections(): List<NotificationSectionUiState> =
    NotificationCategory.entries.map { category ->
        NotificationSectionUiState(
            category = category,
            enabled = true,
            detectionStandardTime = if (category == NotificationCategory.Inactivity) "4시간" else null
        )
    }

private fun dangerNotificationSections(): List<NotificationSectionUiState> = listOf(
    NotificationSectionUiState(
        category = NotificationCategory.Sos,
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오늘 오전 10:00",
                title = "어머니 · 경기도 하남시 창우동",
                severity = NotificationSeverity.Danger,
                occurredAtMillis = System.currentTimeMillis()
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Inactivity,
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "어제 오전 10:00부터",
                title = "4시간 미사용 감지됨",
                severity = NotificationSeverity.Danger,
                occurredAtMillis = System.currentTimeMillis()
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.RiskLink,
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오늘 오전 10:00",
                title = "http://fake-bank.xyz",
                severity = NotificationSeverity.Danger,
                occurredAtMillis = System.currentTimeMillis()
            )
        )
    ),
    NotificationSectionUiState(
        category = NotificationCategory.Outing,
        enabled = true,
        messages = listOf(
            NotificationMessageUiState(
                time = "오늘 오전 10:00",
                title = "외출 나가셨어요",
                severity = NotificationSeverity.Empty,
                tintBackground = false,
                occurredAtMillis = System.currentTimeMillis()
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
            uiState = NotificationScreenUiState(
                sections = emptyNotificationSections(),
                footerPanel = NotificationFooterPanelUiState(
                    tone = NotificationFooterTone.Recommendation
                )
            )
        )
    }
}

@Preview(
    name = "Notification Disconnected",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun NotificationDisconnectedPreview() {
    SENIOR_ONTheme {
        NotificationScreen(
            uiState = NotificationScreenUiState(
                sections = emptyNotificationSections(),
                footerPanel = NotificationFooterPanelUiState(
                    tone = NotificationFooterTone.Warning
                )
            )
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
