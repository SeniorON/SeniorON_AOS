package com.example.senior_on.ui.notification

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

enum class NotificationSeverity {
    Empty,
    Normal,
    Danger
}

enum class NotificationCategory {
    Sos,
    Inactivity,
    RiskLink,
    Outing
}

data class NotificationSectionUiState(
    val category: NotificationCategory,
    val title: String,
    val description: String,
    val enabled: Boolean,
    val messages: List<NotificationMessageUiState> = emptyList()
)

data class NotificationMessageUiState(
    val time: String,
    val title: String,
    val detail: String? = null,
    val severity: NotificationSeverity,
    val tintBackground: Boolean = true
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
            .height(62.dp)
            .padding(start = 16.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "알림",
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.weight(1f))

        NotificationSummaryChip(
            severity = severity,
            count = count
        )

        Spacer(modifier = Modifier.width(8.dp))

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
        NotificationSeverity.Empty -> SeniorOnColors.White
        NotificationSeverity.Normal -> SeniorOnColors.White
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
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = SeniorOnTextStyles.CaptionMedium,
            color = contentColor
        )
    }
}

@Composable
private fun NotificationMoodButton(
    severity: NotificationSeverity,
    modifier: Modifier = Modifier
) {
    val containerColor = when (severity) {
        NotificationSeverity.Empty -> SeniorOnColors.Gray200
        NotificationSeverity.Normal -> SeniorOnColors.Primary200
        NotificationSeverity.Danger -> SeniorOnColors.Red200
    }
    val contentColor = when (severity) {
        NotificationSeverity.Empty -> SeniorOnColors.Gray600
        NotificationSeverity.Normal -> SeniorOnColors.Primary600
        NotificationSeverity.Danger -> SeniorOnColors.White
    }
    val face = when (severity) {
        NotificationSeverity.Empty -> "··"
        NotificationSeverity.Normal -> "⌣"
        NotificationSeverity.Danger -> "··"
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = face,
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun NotificationSectionCard(
    section: NotificationSectionUiState,
    showDetailArrow: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            if (showDetailArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp),
                    tint = SeniorOnColors.Gray500
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NotificationSwitch(
                checked = section.enabled,
                severity = if (section.category == NotificationCategory.Sos) {
                    NotificationSeverity.Danger
                } else {
                    NotificationSeverity.Normal
                }
            )
        }

        if (section.messages.isEmpty()) {
            Spacer(modifier = Modifier.height(if (showDetailArrow) 12.dp else 4.dp))

            if (showDetailArrow) {
                NotificationEmptyMessage(section = section)
            } else {
                Text(
                    text = section.description,
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Gray400
                )
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                section.messages.forEach { message ->
                    NotificationMessageRow(message = message)
                }
            }
        }
    }
}

@Composable
private fun NotificationSwitch(
    checked: Boolean,
    severity: NotificationSeverity,
    modifier: Modifier = Modifier
) {
    val trackColor = when {
        !checked -> SeniorOnColors.Gray200
        severity == NotificationSeverity.Danger -> SeniorOnColors.Red300
        else -> SeniorOnColors.Primary600
    }

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(SeniorOnColors.White)
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
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Background1)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Gray300
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "감지된 ${section.title} 알림이 없어요",
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray400
        )
    }
}

@Composable
private fun NotificationMessageRow(
    message: NotificationMessageUiState,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        !message.tintBackground -> Color(0xFFF2F5F8)
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Red100
        else -> SeniorOnColors.Primary100
    }
    val iconColor = when (message.severity) {
        NotificationSeverity.Danger -> SeniorOnColors.Red300
        NotificationSeverity.Normal -> SeniorOnColors.Primary600
        NotificationSeverity.Empty -> SeniorOnColors.Gray500
    }
    val textColor = when {
        message.severity == NotificationSeverity.Danger -> SeniorOnColors.Red300
        else -> SeniorOnColors.Gray800
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(containerColor)
            .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconColor
        )

        Spacer(modifier = Modifier.width(9.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message.time,
                style = SeniorOnTextStyles.BodySSemiBold,
                color = textColor
            )
            Text(
                text = message.title,
                style = SeniorOnTextStyles.CaptionMedium,
                color = textColor
            )
            message.detail?.let { detail ->
                Text(
                    text = detail,
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = textColor
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_next),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (message.severity == NotificationSeverity.Danger) {
                SeniorOnColors.Red300
            } else {
                SeniorOnColors.Gray500
            }
        )
    }
}

@Composable
internal fun NotificationEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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
internal fun SeniorOnBottomNavigation(
    selectedItem: SeniorOnMainTab,
    onItemClick: (SeniorOnMainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .navigationBarsPadding()
            .padding(top = 9.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeniorOnMainTab.values().forEach { item ->
            SeniorOnBottomNavigationItem(
                item = item,
                selected = item == selectedItem,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun SeniorOnBottomNavigationItem(
    item: SeniorOnMainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray300

    Column(
        modifier = modifier
            .width(48.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = item.iconResId),
            contentDescription = item.label,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            style = SeniorOnTextStyles.CaptionMedium,
            color = contentColor
        )
    }
}

enum class SeniorOnMainTab(
    val label: String,
    @DrawableRes val iconResId: Int
) {
    Screen("화면", R.drawable.ic_nav_phone),
    Health("건강", R.drawable.ic_nav_health),
    Notification("알림", R.drawable.ic_nav_notification),
    Family("가족", R.drawable.ic_nav_family),
    Setting("설정", R.drawable.ic_nav_setting)
}
