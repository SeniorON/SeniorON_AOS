package com.example.senior_on.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private const val RECENT_ALARM_WINDOW_MILLIS = 48L * 60L * 60L * 1000L

enum class NotificationSeverity {
    Empty,
    Normal,
    Danger
}

enum class NotificationCategory(
    val displayTitle: String,
    val displayDescription: String
) {
    Sos(
        displayTitle = "SOS 알림",
        displayDescription = "부모님의 긴급 도움 요청을 알려드려요."
    ),
    Inactivity(
        displayTitle = "무활동 감지",
        displayDescription = "부모님의 활동 상태를 확인할 수 있어요."
    ),
    RiskLink(
        displayTitle = "위험 링크 감지",
        displayDescription = "의심되는 링크 접근을 알려드려요."
    ),
    Outing(
        displayTitle = "외출·귀가 알림",
        displayDescription = "부모님의 외출 및 귀가를 확인할 수 있어요."
    )
}

data class NotificationSectionUiState(
    val category: NotificationCategory,
    val title: String = category.displayTitle,
    val description: String = category.displayDescription,
    val enabled: Boolean,
    val detectionStandardTime: String? = null,
    val messages: List<NotificationMessageUiState> = emptyList()
)

data class NotificationMessageUiState(
    val time: String,
    val title: String,
    val detail: String? = null,
    val severity: NotificationSeverity,
    val tintBackground: Boolean = true,
    val occurredAtMillis: Long? = null
)

data class NotificationScreenUiState(
    val sections: List<NotificationSectionUiState>,
    val footerPanel: NotificationFooterPanelUiState? = null,
    val hasHomeAddress: Boolean = true,
    val isParentPhoneInternetConnected: Boolean = true
)

enum class NotificationFooterTone {
    Recommendation,
    Warning
}

data class NotificationFooterPanelUiState(
    val tone: NotificationFooterTone,
    val title: String = when (tone) {
        NotificationFooterTone.Recommendation -> "모든 알림이 꺼져 있어요."
        NotificationFooterTone.Warning -> "어머니 폰이 연결되지 않았어요."
    },
    val highlightedTitle: String? = when (tone) {
        NotificationFooterTone.Recommendation -> "SOS 등 중요 알림 설정"
        NotificationFooterTone.Warning -> null
    },
    val titleSuffix: String = when (tone) {
        NotificationFooterTone.Recommendation -> "을 권장해요."
        NotificationFooterTone.Warning -> ""
    },
    val description: String = when (tone) {
        NotificationFooterTone.Recommendation -> "알림을 켜두면 위험 상황이나 중요한 일을\n더 빠르게 확인할 수 있어요."
        NotificationFooterTone.Warning -> "가족 코드를 입력해 연결하면 알림 기능을\n설정할 수 있어요."
    }
)

@Composable
internal fun NotificationTopBar(
    severity: NotificationSeverity,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "알림",
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.weight(1f))

        NotificationSummaryChip(
            severity = severity,
            count = count
        )

        Spacer(modifier = Modifier.width(4.dp))

        NotificationMoodButton(severity = severity)
    }
}

@Composable
private fun NotificationSummaryChip(
    severity: NotificationSeverity,
    count: Int,
    modifier: Modifier = Modifier
) {
    val containerColor = when (severity) {
        NotificationSeverity.Empty -> SeniorOnColors.Gray100
        NotificationSeverity.Normal -> SeniorOnColors.Primary200
        NotificationSeverity.Danger -> SeniorOnColors.Red100
    }
    val contentColor = when (severity) {
        NotificationSeverity.Empty -> SeniorOnColors.Gray500
        NotificationSeverity.Normal -> SeniorOnColors.Primary600
        NotificationSeverity.Danger -> SeniorOnColors.Red300
    }
    val label = when (severity) {
        NotificationSeverity.Empty -> "받고있는 알림: 0개"
        NotificationSeverity.Normal -> "받고있는 알림: ${count}개"
        NotificationSeverity.Danger -> "위험 알림: ${count}개"
    }

    Box(
        modifier = modifier
            .height(25.dp)
            .clip(RoundedCornerShape(98.dp))
            .background(containerColor)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = SeniorOnTextStyles.BodySMedium,
            color = contentColor
        )
    }
}

@Composable
private fun NotificationMoodButton(
    severity: NotificationSeverity,
    modifier: Modifier = Modifier
) {
    val iconResId = when (severity) {
        NotificationSeverity.Empty -> R.drawable.ic_illust_neutral
        NotificationSeverity.Normal -> R.drawable.ic_illust_happy
        NotificationSeverity.Danger -> R.drawable.ic_illustwarning
    }

    Image(
        painter = painterResource(id = iconResId),
        contentDescription = null,
        modifier = modifier.size(40.dp)
    )
}

@Composable
internal fun NotificationSectionCard(
    section: NotificationSectionUiState,
    showDetailArrow: Boolean,
    modifier: Modifier = Modifier,
    textMuted: Boolean = false,
    onClick: () -> Unit = {},
    onToggleClick: () -> Unit = {},
    onDetectionTimeClick: () -> Unit = {}
) {
    val titleColor = if (textMuted) SeniorOnColors.Gray400 else SeniorOnColors.Gray800
    val descriptionColor = if (textMuted) SeniorOnColors.Gray300 else SeniorOnColors.Gray500
    val arrowAlpha by animateFloatAsState(
        targetValue = if (showDetailArrow) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "NotificationArrowAlpha"
    )
    val visibleMessages = if (section.enabled) {
        section.messages
            .filter { message -> message.isRecentAlarm() }
            .sortedByDescending { message -> message.occurredAtMillis ?: Long.MAX_VALUE }
            .take(1)
    } else {
        emptyList()
    }
    val hasDetectionStandardTime =
        section.category == NotificationCategory.Inactivity &&
            section.enabled &&
            section.detectionStandardTime != null
    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 220))
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                top = 10.dp,
                bottom = if (hasDetectionStandardTime) 0.dp else 10.dp
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = SeniorOnTextStyles.BodyLBold,
                    color = titleColor
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp)
                        .alpha(arrowAlpha),
                    tint = SeniorOnColors.Gray500
                )
            }

            if (visibleMessages.isEmpty()) {
                Spacer(modifier = Modifier.height(if (showDetailArrow) 12.dp else 4.dp))

                AnimatedVisibility(
                    visible = showDetailArrow,
                    enter = fadeIn(animationSpec = tween(durationMillis = 160)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 120))
                ) {
                    NotificationEmptyMessage(section = section)
                }

                AnimatedVisibility(
                    visible = !showDetailArrow,
                    enter = fadeIn(animationSpec = tween(durationMillis = 160)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 120))
                ) {
                    Text(
                        text = section.description,
                        modifier = Modifier.padding(end = 60.dp),
                        style = SeniorOnTextStyles.BodySMedium,
                        color = descriptionColor
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleMessages.forEach { message ->
                        NotificationMessageRow(
                            category = section.category,
                            message = message
                        )
                    }
                }
            }

            NotificationDetectionStandardTime(
                section = section,
                onClick = onDetectionTimeClick
            )
        }

        NotificationSwitch(
            checked = section.enabled,
            severity = if (section.category == NotificationCategory.Sos) {
                NotificationSeverity.Danger
            } else {
                NotificationSeverity.Normal
            },
            onClick = onToggleClick,
            modifier = Modifier.align(Alignment.TopEnd)
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

@Composable
private fun NotificationDetectionStandardTime(
    section: NotificationSectionUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val standardTime = section.detectionStandardTime
    if (
        section.category != NotificationCategory.Inactivity ||
        !section.enabled ||
        standardTime == null
    ) {
        return
    }

    Spacer(modifier = Modifier.height(2.dp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "감지 기준 시간",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray400
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = standardTime,
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Primary600
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = SeniorOnColors.Gray400
        )
    }
}

@Composable
private fun NotificationSwitch(
    checked: Boolean,
    severity: NotificationSeverity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = when {
        !checked -> SeniorOnColors.Gray200
        severity == NotificationSeverity.Danger -> SeniorOnColors.Red300
        else -> SeniorOnColors.Primary600
    }
    val knobOffsetX by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "NotificationSwitchKnobOffsetX"
    )

    Box(
        modifier = modifier
            .size(width = 46.dp, height = 26.dp)
            .clip(RoundedCornerShape(98.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .offset(x = knobOffsetX)
                .clip(CircleShape)
                .background(SeniorOnColors.SupportWhite100)
        )
    }
}

@Composable
private fun NotificationEmptyMessage(
    section: NotificationSectionUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background2)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray300
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = "감지된 ${section.title} 알림이 없어요",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray300
        )
    }
}

@Composable
private fun NotificationMessageRow(
    category: NotificationCategory,
    message: NotificationMessageUiState,
    modifier: Modifier = Modifier
) {
    val isSosDanger = category == NotificationCategory.Sos &&
        message.severity == NotificationSeverity.Danger
    val containerColor = when {
        isSosDanger -> SeniorOnColors.Red300
        category == NotificationCategory.Outing -> SeniorOnColors.Background5
        !message.tintBackground -> Color(0xFFF2F5F8)
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Red100
        else -> SeniorOnColors.Primary100
    }
    val iconColor = when {
        isSosDanger -> SeniorOnColors.SupportWhite100
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Gray800
        !message.tintBackground -> SeniorOnColors.Gray800
        message.severity == NotificationSeverity.Normal -> SeniorOnColors.Primary600
        else -> SeniorOnColors.Gray500
    }
    val timeColor = if (isSosDanger) {
        SeniorOnColors.SupportWhite100
    } else {
        SeniorOnColors.Gray800
    }
    val titleColor = when {
        isSosDanger -> SeniorOnColors.SupportWhite100
        category == NotificationCategory.Outing -> SeniorOnColors.Schedule
        !message.tintBackground -> SeniorOnColors.ActionAdd
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Red300
        else -> SeniorOnColors.Gray800
    }
    val detailColor = when {
        isSosDanger -> SeniorOnColors.SupportWhite100
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Red300
        else -> SeniorOnColors.Gray800
    }
    val arrowColor = when {
        isSosDanger -> SeniorOnColors.SupportWhite100
        else -> SeniorOnColors.Gray700
    }
    val iconResId = when {
        category == NotificationCategory.Outing -> R.drawable.ic_out
        else -> R.drawable.ic_alert_filled
    }
    val titleTextDecoration = if (category == NotificationCategory.RiskLink) {
        TextDecoration.Underline
    } else {
        TextDecoration.None
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(containerColor)
            .padding(start = 10.dp, end = 10.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message.time,
                style = SeniorOnTextStyles.BodyMBold,
                color = timeColor
            )
            Text(
                text = message.title,
                style = SeniorOnTextStyles.BodySMedium,
                color = titleColor,
                textDecoration = titleTextDecoration
            )
            message.detail?.let { detail ->
                Text(
                    text = detail,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = detailColor
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = arrowColor
        )
    }
}

@Composable
internal fun NotificationEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_illust_happy),
            contentDescription = null,
            modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(13.dp))

        Text(
            text = buildAnnotatedString {
                append("모든 알림이 꺼져 있어요.\n")
                withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                    append("SOS 등 중요 알림 설정")
                }
                append("을 권장해요.")
            },
            style = SeniorOnTextStyles.BodySSemiBold,
            color = SeniorOnColors.Gray800,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "알림을 켜두면 위험 상황이나 중요한 일을\n더 빠르게 확인할 수 있어요.",
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Gray400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun NotificationFooterPanel(
    uiState: NotificationFooterPanelUiState,
    modifier: Modifier = Modifier
) {
    val containerColor = when (uiState.tone) {
        NotificationFooterTone.Recommendation -> SeniorOnColors.Background2
        NotificationFooterTone.Warning -> SeniorOnColors.Red100
    }
    val titleColor = when (uiState.tone) {
        NotificationFooterTone.Recommendation -> SeniorOnColors.Gray800
        NotificationFooterTone.Warning -> SeniorOnColors.Red300
    }
    val highlightColor = when (uiState.tone) {
        NotificationFooterTone.Recommendation -> SeniorOnColors.Primary600
        NotificationFooterTone.Warning -> SeniorOnColors.Red300
    }
    val descriptionColor = when (uiState.tone) {
        NotificationFooterTone.Recommendation -> SeniorOnColors.Gray400
        NotificationFooterTone.Warning -> SeniorOnColors.Red300
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(containerColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.tone == NotificationFooterTone.Warning) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_alert_filled),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.Red300
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_illust_alert),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(
            modifier = Modifier.height(
                if (uiState.tone == NotificationFooterTone.Warning) 12.dp else 6.dp
            )
        )

        Text(
            text = buildAnnotatedString {
                append(uiState.title)
                uiState.highlightedTitle?.let { highlightedTitle ->
                    append("\n")
                    withStyle(SpanStyle(color = highlightColor)) {
                        append(highlightedTitle)
                    }
                    append(uiState.titleSuffix)
                }
            },
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = titleColor,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.height(
                if (uiState.tone == NotificationFooterTone.Warning) 8.dp else 12.dp
            )
        )

        Text(
            text = uiState.description,
            style = SeniorOnTextStyles.CaptionRegular,
            color = descriptionColor,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(
    name = "Top Bar - Empty",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
private fun NotificationTopBarEmptyPreview() {
    SENIOR_ONTheme {
        NotificationTopBar(
            severity = NotificationSeverity.Empty,
            count = 0
        )
    }
}

@Preview(
    name = "Top Bar - Normal",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
private fun NotificationTopBarNormalPreview() {
    SENIOR_ONTheme {
        NotificationTopBar(
            severity = NotificationSeverity.Normal,
            count = 4
        )
    }
}

@Preview(
    name = "Top Bar - Danger",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
private fun NotificationTopBarDangerPreview() {
    SENIOR_ONTheme {
        NotificationTopBar(
            severity = NotificationSeverity.Danger,
            count = 4
        )
    }
}

@Preview(
    name = "Section Cards - Summary",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardPreview() {
    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NotificationSectionCard(
                section = NotificationSectionUiState(
                    category = NotificationCategory.Sos,
                    enabled = false
                ),
                showDetailArrow = false
            )

            NotificationSectionCard(
                section = NotificationSectionUiState(
                    category = NotificationCategory.Inactivity,
                    enabled = true,
                    detectionStandardTime = "4시간"
                ),
                showDetailArrow = true
            )

            NotificationSectionCard(
                section = NotificationSectionUiState(
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
                showDetailArrow = true
            )
        }
    }
}

@Preview(
    name = "Section Card - Disabled",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardDisabledPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
                category = NotificationCategory.Sos,
                enabled = false
            ),
            showDetailArrow = false
        )
    }
}

@Preview(
    name = "Section Card - Disconnected",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardDisconnectedPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
                category = NotificationCategory.Inactivity,
                enabled = false
            ),
            showDetailArrow = false,
            textMuted = true
        )
    }
}

@Preview(
    name = "Section Card - Enabled Empty",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardEnabledEmptyPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
                category = NotificationCategory.Inactivity,
                enabled = true,
                detectionStandardTime = "4시간"
            ),
            showDetailArrow = true
        )
    }
}

@Preview(
    name = "Section Card - SOS Danger",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardSosDangerPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
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
            showDetailArrow = true
        )
    }
}

@Preview(
    name = "Section Card - Risk Link Danger",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardRiskLinkDangerPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
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
            showDetailArrow = true
        )
    }
}

@Preview(
    name = "Section Card - Outing Normal",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardOutingNormalPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
                category = NotificationCategory.Outing,
                enabled = true,
                messages = listOf(
                    NotificationMessageUiState(
                        time = "오늘 오전 10:00",
                        title = "외출 나가셨어요",
                        severity = NotificationSeverity.Normal,
                        tintBackground = false,
                        occurredAtMillis = System.currentTimeMillis()
                    )
                )
            ),
            showDetailArrow = true
        )
    }
}

@Preview(
    name = "Section Card - Expired Alarm",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationSectionCardExpiredAlarmPreview() {
    NotificationPreviewBox {
        NotificationSectionCard(
            section = NotificationSectionUiState(
                category = NotificationCategory.RiskLink,
                enabled = true,
                messages = listOf(
                    NotificationMessageUiState(
                        time = "이틀 전 오전 10:00",
                        title = "http://fake-bank.xyz",
                        severity = NotificationSeverity.Danger,
                        occurredAtMillis = System.currentTimeMillis() - (49L * 60L * 60L * 1000L)
                    )
                )
            ),
            showDetailArrow = true
        )
    }
}

@Preview(
    name = "Footer Panel - Recommendation",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationFooterPanelRecommendationPreview() {
    NotificationPreviewBox {
        NotificationFooterPanel(
            uiState = NotificationFooterPanelUiState(
                tone = NotificationFooterTone.Recommendation
            ),
            modifier = Modifier.height(200.dp)
        )
    }
}

@Preview(
    name = "Footer Panel - Warning",
    showBackground = true,
    backgroundColor = 0xFFF8F8F5,
    widthDp = 360
)
@Composable
private fun NotificationFooterPanelWarningPreview() {
    NotificationPreviewBox {
        NotificationFooterPanel(
            uiState = NotificationFooterPanelUiState(
                tone = NotificationFooterTone.Warning
            ),
            modifier = Modifier.height(200.dp)
        )
    }
}

@Composable
private fun NotificationPreviewBox(
    content: @Composable () -> Unit
) {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.SupportWhite100)
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}
