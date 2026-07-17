package com.example.senior_on.ui.notification

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NotificationDetailScreen(
    category: NotificationCategory,
    message: NotificationMessageUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onDirectionsClick: () -> Unit = {}
) {
    BackHandler(onBack = onBackClick)

    val detail = remember(category, message) {
        NotificationDetailUiState.from(category, message)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(detail.heroBrush)
    ) {
        NotificationDetailHero(
            detail = detail,
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxSize()
        )

        if (category == NotificationCategory.RiskLink) {
            RiskLinkDetailSheet(
                detail = detail,
                onRefreshClick = onRefreshClick,
                onCallClick = onCallClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else {
            LocationDetailSheet(
                detail = detail,
                onRefreshClick = onRefreshClick,
                onCallClick = onCallClick,
                onDirectionsClick = onDirectionsClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun NotificationDetailHero(
    detail: NotificationDetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentTopPadding = when (detail.category) {
        NotificationCategory.Sos,
        NotificationCategory.Inactivity -> 84.dp
        NotificationCategory.Outing -> 90.dp
        NotificationCategory.RiskLink -> 154.dp
    }

    Box(modifier = modifier.statusBarsPadding()) {
        if (detail.showRipple) {
            NotificationRippleBackground(
                category = detail.category,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 14.dp)
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
                tint = detail.backIconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = contentTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = detail.heroIconResId),
                contentDescription = null,
                tint = detail.heroContentColor,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = detail.heroTitle,
                style = SeniorOnTextStyles.HeadingM,
                color = detail.heroContentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = detail.heroTime,
                style = SeniorOnTextStyles.BodySMedium,
                color = detail.heroContentColor
            )
        }
    }
}

@Composable
private fun NotificationRippleBackground(
    category: NotificationCategory,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "NotificationDetailRipple")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = RIPPLE_CYCLE_DURATION_MILLIS,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "NotificationDetailRipplePhase"
    )
    val rippleOvals = remember(category) {
        when (category) {
            NotificationCategory.Sos -> listOf(
                centeredRippleOval(177.60f, 174.00f, SOS_RIPPLE_CENTER_Y, Color(0x33FFFFFF)),
                centeredRippleOval(235.03f, 230.27f, SOS_RIPPLE_CENTER_Y, Color(0x1FFFFFFF)),
                centeredRippleOval(296f, 290f, SOS_RIPPLE_CENTER_Y, Color(0x0FFFFFFF))
            )
            NotificationCategory.Inactivity -> listOf(
                centeredRippleOval(191.40f, 191.40f, INACTIVITY_RIPPLE_CENTER_Y, Color(0xFFFFD1D1)),
                centeredRippleOval(253.30f, 253.30f, INACTIVITY_RIPPLE_CENTER_Y, Color(0xFFFFDDDD)),
                centeredRippleOval(319f, 319f, INACTIVITY_RIPPLE_CENTER_Y, Color(0xFFFFE5E5))
            )
            NotificationCategory.RiskLink -> listOf(
                centeredRippleOval(191.40f, 191.40f, RISK_LINK_RIPPLE_CENTER_Y, Color(0xFFFFD1D1)),
                centeredRippleOval(253.30f, 253.30f, RISK_LINK_RIPPLE_CENTER_Y, Color(0xFFFFDDDD)),
                centeredRippleOval(319f, 319f, RISK_LINK_RIPPLE_CENTER_Y, Color(0xFFFFE5E5))
            )
            NotificationCategory.Outing -> emptyList()
        }
    }

    Canvas(modifier = modifier) {
        val scale = size.width / RIPPLE_REFERENCE_WIDTH
        val elapsedMillis = phase * RIPPLE_CYCLE_DURATION_MILLIS
        rippleOvals.forEachIndexed { index, oval ->
            val localProgress = (
                elapsedMillis - index * RIPPLE_STAGGER_MILLIS
            ) / RIPPLE_WAVE_DURATION_MILLIS
            if (localProgress !in 0f..1f) return@forEachIndexed

            val progress = LinearOutSlowInEasing.transform(localProgress)
            val expansion = 1f + RIPPLE_EXPANSION_RATIO * progress
            val animatedWidth = oval.width * expansion * scale
            val animatedHeight = oval.height * expansion * scale
            val center = Offset(
                x = (oval.left + oval.width / 2f) * scale,
                y = (oval.top + oval.height / 2f) * scale
            )
            drawOval(
                color = oval.color.copy(alpha = oval.color.alpha * (1f - localProgress)),
                topLeft = Offset(
                    x = center.x - animatedWidth / 2f,
                    y = center.y - animatedHeight / 2f
                ),
                size = Size(animatedWidth, animatedHeight),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private data class NotificationRippleOval(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val color: Color
)

private fun centeredRippleOval(
    width: Float,
    height: Float,
    centerY: Float,
    color: Color
) = NotificationRippleOval(
    left = (RIPPLE_REFERENCE_WIDTH - width) / 2f,
    top = centerY - height / 2f,
    width = width,
    height = height,
    color = color
)

private const val RIPPLE_REFERENCE_WIDTH = 360f
private const val RIPPLE_EXPANSION_RATIO = 0.08f
private const val RIPPLE_CYCLE_DURATION_MILLIS = 2400
private const val RIPPLE_WAVE_DURATION_MILLIS = 1600f
private const val RIPPLE_STAGGER_MILLIS = 400f
private const val HERO_CONTENT_CENTER_OFFSET = 51.6f
private const val SOS_RIPPLE_CENTER_Y = 84f + HERO_CONTENT_CENTER_OFFSET
private const val INACTIVITY_RIPPLE_CENTER_Y = 84f + HERO_CONTENT_CENTER_OFFSET
private const val RISK_LINK_RIPPLE_CENTER_Y = 154f + HERO_CONTENT_CENTER_OFFSET

@Composable
private fun LocationDetailSheet(
    detail: NotificationDetailUiState,
    onRefreshClick: () -> Unit,
    onCallClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(395.dp)
            .clip(RoundedCornerShape(topStart = SeniorOnRadius.XLarge, topEnd = SeniorOnRadius.XLarge))
            .background(SeniorOnColors.SupportWhite100)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 18.dp)
    ) {
        DetailSheetHeader(
            iconResId = R.drawable.ic_location,
            title = if (detail.category == NotificationCategory.Sos) "현재 위치" else "위치",
            onRefreshClick = onRefreshClick
        )

        Text(
            text = detail.address,
            style = SeniorOnTextStyles.HeadingS,
            color = SeniorOnColors.Black,
            modifier = Modifier.padding(start = 28.dp, top = 5.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotificationMapPlaceholder()

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.Gray50)
        ) {
            detail.inactivityHours?.let { hours ->
                DetailInformationRow(
                    iconResId = R.drawable.ic_big_clock,
                    label = "무활동 감지 시간",
                    value = hours,
                    showBottomBorder = true
                )
            }
            DetailInformationRow(
                iconResId = R.drawable.ic_battery,
                label = "기기 배터리",
                value = detail.battery,
                showBottomBorder = detail.inactivityHours == null
            )
            if (detail.inactivityHours == null) {
                DetailInformationRow(
                    iconResId = R.drawable.ic_share_location,
                    label = "마지막 위치 업데이트",
                    value = detail.lastLocationUpdate
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DetailActionButton(
            iconResId = R.drawable.ic_call,
            label = "전화 걸기",
            containerColor = detail.actionColor,
            contentColor = SeniorOnColors.SupportWhite100,
            onClick = onCallClick
        )

        Spacer(modifier = Modifier.height(6.dp))

        DetailActionButton(
            iconResId = R.drawable.ic_direction,
            label = "길 찾기",
            containerColor = SeniorOnColors.SupportWhite100,
            contentColor = SeniorOnColors.Gray800,
            outlined = true,
            onClick = onDirectionsClick
        )
    }
}

@Composable
private fun RiskLinkDetailSheet(
    detail: NotificationDetailUiState,
    onRefreshClick: () -> Unit,
    onCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(271.dp)
            .clip(RoundedCornerShape(topStart = SeniorOnRadius.XLarge, topEnd = SeniorOnRadius.XLarge))
            .background(SeniorOnColors.SupportWhite100)
            .padding(16.dp)
    ) {
        DetailSheetHeader(
            iconResId = R.drawable.ic_link,
            title = "감지된 URL",
            onRefreshClick = onRefreshClick
        )

        Text(
            text = detail.url,
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Red300,
            textDecoration = TextDecoration.Underline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        DetailInformationRow(
            label = "위험도",
            value = "높음",
            valueColor = SeniorOnColors.Red300,
            labelStyle = SeniorOnTextStyles.BodyMSemiBold,
            startPadding = 30.dp,
            endPadding = 0.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        DetailInformationRow(
            iconResId = R.drawable.ic_big_clock,
            label = "감지 시간",
            value = detail.detectedTime,
            modifier = Modifier
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.Gray50)
        )

        Spacer(modifier = Modifier.weight(1f))

        DetailActionButton(
            iconResId = R.drawable.ic_call,
            label = "전화 걸기",
            containerColor = SeniorOnColors.Red400,
            contentColor = SeniorOnColors.SupportWhite100,
            onClick = onCallClick
        )
    }
}

@Composable
private fun DetailSheetHeader(
    iconResId: Int,
    title: String,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = SeniorOnColors.Red300,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray700,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_refresh),
            contentDescription = "새로고침",
            tint = SeniorOnColors.Gray600,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onRefreshClick)
        )
    }
}

@Composable
private fun NotificationMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(102.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background5)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val roadColor = SeniorOnColors.SupportWhite100
            drawLine(
                color = roadColor,
                start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.7f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.2f),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = SeniorOnColors.Primary300.copy(alpha = 0.75f),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = SeniorOnColors.Gray200,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.48f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "현재 위치",
            tint = SeniorOnColors.Red300,
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
        )
    }
}

@Composable
private fun DetailInformationRow(
    label: String,
    value: String,
    iconResId: Int? = null,
    valueColor: Color = SeniorOnColors.Gray800,
    labelStyle: TextStyle = SeniorOnTextStyles.BodySMedium,
    startPadding: Dp = 8.dp,
    endPadding: Dp = 8.dp,
    showBottomBorder: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPadding, end = endPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = SeniorOnColors.Gray800,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = labelStyle,
                color = SeniorOnColors.Gray800,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = SeniorOnTextStyles.BodySMedium,
                color = valueColor
            )
        }

        if (showBottomBorder) {
            Spacer(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SeniorOnColors.Gray100)
            )
        }
    }
}

@Composable
private fun DetailActionButton(
    iconResId: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(
                if (outlined) {
                    Modifier.background(SeniorOnColors.Gray200, shape).padding(1.dp)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = SeniorOnTextStyles.ButtonM, color = contentColor)
        }
    }
}

private data class NotificationDetailUiState(
    val category: NotificationCategory,
    val heroTitle: String,
    val heroTime: String,
    val heroIconResId: Int,
    val heroBrush: Brush,
    val heroContentColor: Color,
    val backIconColor: Color,
    val showRipple: Boolean,
    val actionColor: Color,
    val address: String = "경기도 하남시 창우동",
    val battery: String = "48%",
    val lastLocationUpdate: String = "2분전",
    val inactivityHours: String? = null,
    val url: String = "http://fake-bank.xyz",
    val detectedTime: String = "오후 5:00"
) {
    companion object {
        fun from(
            category: NotificationCategory,
            message: NotificationMessageUiState
        ): NotificationDetailUiState {
            val isReturnedHome = message.movementType == NotificationMovementType.ReturnedHome
            val formattedTime = message.occurredAtMillis.toKoreanDateTime()
            return when (category) {
                NotificationCategory.Sos -> NotificationDetailUiState(
                    category = category,
                    heroTitle = "도움이 필요해요",
                    heroTime = formattedTime,
                    heroIconResId = R.drawable.ic_big_alert_filled,
                    heroBrush = Brush.verticalGradient(listOf(SeniorOnColors.Red200, SeniorOnColors.Red300)),
                    heroContentColor = SeniorOnColors.SupportWhite100,
                    backIconColor = SeniorOnColors.SupportWhite100,
                    showRipple = true,
                    actionColor = SeniorOnColors.Red400
                )
                NotificationCategory.Inactivity -> NotificationDetailUiState(
                    category = category,
                    heroTitle = "무활동 감지됨",
                    heroTime = formattedTime,
                    heroIconResId = R.drawable.ic_big_alert_filled,
                    heroBrush = Brush.verticalGradient(listOf(SeniorOnColors.Red100, Color(0xFFFFF5F5))),
                    heroContentColor = SeniorOnColors.DangerSOS,
                    backIconColor = SeniorOnColors.Gray800,
                    showRipple = true,
                    actionColor = SeniorOnColors.Red400,
                    inactivityHours = message.detail ?: "6시간"
                )
                NotificationCategory.RiskLink -> NotificationDetailUiState(
                    category = category,
                    heroTitle = "위험 링크 감지됨",
                    heroTime = formattedTime,
                    heroIconResId = R.drawable.ic_big_alert_filled,
                    heroBrush = Brush.verticalGradient(listOf(SeniorOnColors.Red100, Color(0xFFFFF5F5))),
                    heroContentColor = SeniorOnColors.DangerSOS,
                    backIconColor = SeniorOnColors.Gray800,
                    showRipple = true,
                    actionColor = SeniorOnColors.Red400,
                    url = message.title
                )
                NotificationCategory.Outing -> NotificationDetailUiState(
                    category = category,
                    heroTitle = if (isReturnedHome) "귀가하셨어요" else "외출하셨어요",
                    heroTime = (if (isReturnedHome) "귀가 시간: " else "외출 시간: ") + formattedTime,
                    heroIconResId = if (isReturnedHome) R.drawable.ic_big_in else R.drawable.ic_big_out,
                    heroBrush = if (isReturnedHome) {
                        cssLinearGradient(
                            angleDegrees = 176.04f,
                            startColor = Color(0xFF4F7428),
                            startPosition = -0.032f,
                            endColor = Color(0xFFABE470),
                            endPosition = 0.6286f
                        )
                    } else {
                        cssLinearGradient(
                            angleDegrees = 174.94f,
                            startColor = Color(0xFF486B79),
                            startPosition = -0.0688f,
                            endColor = Color(0xFF769B66),
                            endPosition = 0.4689f
                        )
                    },
                    heroContentColor = SeniorOnColors.SupportWhite100,
                    backIconColor = SeniorOnColors.SupportWhite100,
                    showRipple = false,
                    actionColor = SeniorOnColors.Primary700
                )
            }
        }
    }
}

private fun cssLinearGradient(
    angleDegrees: Float,
    startColor: Color,
    startPosition: Float,
    endColor: Color,
    endPosition: Float
): Brush = object : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val radians = angleDegrees * PI.toFloat() / 180f
        val direction = Offset(
            x = sin(radians),
            y = -cos(radians)
        )
        val gradientLength = abs(direction.x) * size.width + abs(direction.y) * size.height
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseStart = center - direction * (gradientLength / 2f)

        return LinearGradientShader(
            from = baseStart + direction * (gradientLength * startPosition),
            to = baseStart + direction * (gradientLength * endPosition),
            colors = listOf(startColor, endColor)
        )
    }
}

private fun Long?.toKoreanDateTime(): String {
    val dateTime = Instant.ofEpochMilli(this ?: System.currentTimeMillis())
        .atZone(ZoneId.systemDefault())
    return DateTimeFormatter.ofPattern("M월 d일 a h:mm", Locale.KOREAN).format(dateTime)
}

private fun previewDetailMessage(
    movementType: NotificationMovementType? = null,
    title: String = "경기도 하남시 창우동"
) = NotificationMessageUiState(
    time = "오늘 오후 1:25",
    title = title,
    severity = NotificationSeverity.Danger,
    occurredAtMillis = System.currentTimeMillis(),
    movementType = movementType
)

@Preview(name = "Detail - SOS", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationSosDetailPreview() {
    SENIOR_ONTheme {
        NotificationDetailScreen(
            category = NotificationCategory.Sos,
            message = previewDetailMessage(),
            onBackClick = {}
        )
    }
}

@Preview(name = "Detail - Inactivity", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationInactivityDetailPreview() {
    SENIOR_ONTheme {
        NotificationDetailScreen(
            category = NotificationCategory.Inactivity,
            message = previewDetailMessage().copy(detail = "6시간"),
            onBackClick = {}
        )
    }
}

@Preview(name = "Detail - Risk link", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationRiskLinkDetailPreview() {
    SENIOR_ONTheme {
        NotificationDetailScreen(
            category = NotificationCategory.RiskLink,
            message = previewDetailMessage(title = "http://fake-bank.xyz"),
            onBackClick = {}
        )
    }
}

@Preview(name = "Detail - Outing", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationOutingDetailPreview() {
    SENIOR_ONTheme {
        NotificationDetailScreen(
            category = NotificationCategory.Outing,
            message = previewDetailMessage(NotificationMovementType.LeftHome),
            onBackClick = {}
        )
    }
}

@Preview(name = "Detail - Returned home", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun NotificationReturnedHomeDetailPreview() {
    SENIOR_ONTheme {
        NotificationDetailScreen(
            category = NotificationCategory.Outing,
            message = previewDetailMessage(NotificationMovementType.ReturnedHome),
            onBackClick = {}
        )
    }
}
