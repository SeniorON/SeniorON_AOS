package com.example.senior_on.ui.notification

import android.widget.Space
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationHistoryScreen(
    category: NotificationCategory,
    messages: List<NotificationMessageUiState>,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMessageClick: (NotificationMessageUiState) -> Unit = {}
) {
    BackHandler(onBack = onBackClick)

    val groups = remember(messages) { messages.toRecentHistoryGroups() }
    val alarmCount = groups.sumOf { group -> group.messages.size }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
            .statusBarsPadding()
    ) {
        NotificationHistoryTopBar(
            title = category.historyTitle,
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp)
            ) {
                groups.forEachIndexed { index, group ->
                    if (index > 0 && groups[index - 1].isToday) {
                        item(key = "today-divider") {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(SeniorOnColors.Background3)
                            )
                        }
                    }

                    item(key = "label-${group.date}") {
                        Text(
                            text = group.label,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 8.dp
                            ),
                            style = if (group.isToday) {
                                SeniorOnTextStyles.BodyMBold
                            } else {
                                SeniorOnTextStyles.BodySMedium
                            },
                            color = if (group.isToday) {
                                SeniorOnColors.Gray800
                            } else {
                                SeniorOnColors.Gray500
                            }
                        )
                    }

                    items(
                        items = group.messages,
                        key = { message ->
                            "${group.date}-${message.occurredAtMillis}-${message.title}"
                        }
                    ) { message ->
                        NotificationHistoryCard(
                            category = category,
                            message = message,
                            isToday = group.isToday,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            onClick = { onMessageClick(message) }
                        )
                    }
                }
            }

            NotificationHistorySummary(
                count = alarmCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun NotificationHistoryTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800
            )
        }

        Text(
            text = title,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun NotificationHistorySummary(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                append("최근 30일 알림 ")
                withStyle(
                    SeniorOnTextStyles.BodySSemiBold
                        .toSpanStyle()
                        .copy(color = SeniorOnColors.Primary700)
                ) {
                    append("${count}건")
                }
            },
            style = SeniorOnTextStyles.BodySRegular,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun NotificationHistoryCard(
    category: NotificationCategory,
    message: NotificationMessageUiState,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isTodaySos = isToday && category == NotificationCategory.Sos
    val containerColor = when {
        isTodaySos -> SeniorOnColors.Red300
        isToday && category == NotificationCategory.Inactivity -> SeniorOnColors.Red100
        isToday && category == NotificationCategory.RiskLink -> SeniorOnColors.Red100
        isToday && category == NotificationCategory.Outing -> SeniorOnColors.Background5
        else -> SeniorOnColors.Background3
    }
    val iconColor = if (isTodaySos) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray800
    val timeColor = if (isTodaySos) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray800
    val detailColor = when {
        isTodaySos -> SeniorOnColors.SupportWhite100
        isToday && category == NotificationCategory.Inactivity -> SeniorOnColors.Red300
        isToday && category == NotificationCategory.RiskLink -> SeniorOnColors.Red300
        isToday && category == NotificationCategory.Outing -> SeniorOnColors.Schedule
        else -> SeniorOnColors.Gray600
    }
    val arrowColor = if (isTodaySos) SeniorOnColors.SupportWhite100 else SeniorOnColors.Gray700
    val iconResId = when {
        category == NotificationCategory.Outing &&
            message.movementType == NotificationMovementType.ReturnedHome -> R.drawable.ic_in
        category == NotificationCategory.Outing -> R.drawable.ic_out
        else -> R.drawable.ic_alert_filled
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.time,
                style = SeniorOnTextStyles.BodyMBold,
                color = timeColor
            )
            Text(
                text = message.title,
                style = SeniorOnTextStyles.BodySMedium,
                color = detailColor,
                textDecoration = if (category == NotificationCategory.RiskLink) {
                    TextDecoration.Underline
                } else {
                    TextDecoration.None
                }
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = "알림 상세보기",
            modifier = Modifier.size(24.dp),
            tint = arrowColor
        )
    }
}

private data class NotificationHistoryGroup(
    val date: LocalDate,
    val label: String,
    val isToday: Boolean,
    val messages: List<NotificationMessageUiState>
)

private fun List<NotificationMessageUiState>.toRecentHistoryGroups(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<NotificationHistoryGroup> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val oldestDate = today.minusDays(29)
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    return asSequence()
        .mapNotNull { message ->
            val occurredAt = message.occurredAtMillis ?: return@mapNotNull null
            val date = Instant.ofEpochMilli(occurredAt).atZone(zoneId).toLocalDate()
            if (date !in oldestDate..today) return@mapNotNull null
            date to message
        }
        .sortedByDescending { (_, message) -> message.occurredAtMillis }
        .groupBy({ (date, _) -> date }, { (_, message) -> message })
        .map { (date, dateMessages) ->
            NotificationHistoryGroup(
                date = date,
                label = when (date) {
                    today -> "오늘"
                    else -> date.format(formatter)
                },
                isToday = date == today,
                messages = dateMessages
            )
        }
}

private val NotificationCategory.historyTitle: String
    get() = when (this) {
        NotificationCategory.Sos -> "긴급 알림 내역"
        NotificationCategory.Inactivity -> "무활동 감지 내역"
        NotificationCategory.RiskLink -> "위험 링크 감지 내역"
        NotificationCategory.Outing -> "외출·귀가 내역"
    }

@Preview(name = "History - SOS", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SosNotificationHistoryPreview() {
    SENIOR_ONTheme {
        NotificationHistoryScreen(
            category = NotificationCategory.Sos,
            messages = previewHistoryMessages(NotificationCategory.Sos)
        )
    }
}

@Preview(
    name = "History - SOS - No Today",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SosNotificationHistoryWithoutTodayPreview() {
    SENIOR_ONTheme {
        NotificationHistoryScreen(
            category = NotificationCategory.Sos,
            messages = previewHistoryMessages(NotificationCategory.Sos).drop(1)
        )
    }
}

@Preview(name = "History - Inactivity", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun InactivityNotificationHistoryPreview() {
    SENIOR_ONTheme {
        NotificationHistoryScreen(
            category = NotificationCategory.Inactivity,
            messages = previewHistoryMessages(NotificationCategory.Inactivity)
        )
    }
}

@Preview(name = "History - Risk Link", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RiskLinkNotificationHistoryPreview() {
    SENIOR_ONTheme {
        NotificationHistoryScreen(
            category = NotificationCategory.RiskLink,
            messages = previewHistoryMessages(NotificationCategory.RiskLink)
        )
    }
}

@Preview(name = "History - Outing", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OutingNotificationHistoryPreview() {
    SENIOR_ONTheme {
        NotificationHistoryScreen(
            category = NotificationCategory.Outing,
            messages = previewHistoryMessages(NotificationCategory.Outing)
        )
    }
}

private fun previewHistoryMessages(
    category: NotificationCategory,
    nowMillis: Long = System.currentTimeMillis()
): List<NotificationMessageUiState> =
    listOf(0L, 1L, 3L, 5L).mapIndexed { index, daysAgo ->
        NotificationMessageUiState(
            time = when {
                category == NotificationCategory.Inactivity && index == 0 -> "오늘 오전 10:00부터"
                index == 0 -> "오늘 오전 10:00"
                category == NotificationCategory.Outing && index == 3 -> "오후 10:00"
                else -> "오전 10:00"
            },
            title = when (category) {
                NotificationCategory.Sos -> "어머니 · 경기도 하남시 창우동"
                NotificationCategory.Inactivity -> "4시간 미사용 감지됨"
                NotificationCategory.RiskLink -> "http://fake-bank.xyz"
                NotificationCategory.Outing -> if (index == 3) {
                    "귀가하셨어요"
                } else {
                    "외출하셨어요"
                }
            },
            severity = if (category == NotificationCategory.Outing) {
                NotificationSeverity.Normal
            } else {
                NotificationSeverity.Danger
            },
            tintBackground = category != NotificationCategory.Outing,
            occurredAtMillis = nowMillis - daysAgo * PREVIEW_ONE_DAY_MILLIS,
            movementType = when {
                category != NotificationCategory.Outing -> null
                index == 3 -> NotificationMovementType.ReturnedHome
                else -> NotificationMovementType.LeftHome
            }
        )
    }

private const val PREVIEW_ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
