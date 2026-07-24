package com.example.senior_on.ui.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.repository.MockDisplayFixtures
import com.example.senior_on.domain.model.SeniorFontSize
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorPhonePreview(
    configuration: SeniorScreenConfiguration,
    previewWidth: Dp = 116.dp,
    previewHeight: Dp = 263.dp,
    scheduleState: SeniorSchedulePreviewState = SeniorSchedulePreviewState.One,
) {
    val previewScale = previewWidth.value / SeniorPhoneDesignWidth.value
    val previewShape = RoundedCornerShape((24f * previewScale).dp)

    Box(
        modifier = Modifier
            .width(previewWidth)
            .height(previewHeight)
            .clip(previewShape)
            .background(SeniorOnColors.White)
            .border(
                width = (5f * previewScale).dp,
                color = Color(0xFFCEC9D3),
                shape = previewShape,
            ),
        contentAlignment = Alignment.TopStart,
    ) {
        Layout(
            modifier = Modifier.fillMaxSize(),
            content = {
                SeniorPhoneDesign(
                    configuration = configuration,
                    scheduleState = scheduleState,
                )
            },
        ) { measurables, constraints ->
            val designPlaceable = measurables.single().measure(
                Constraints.fixed(
                    width = SeniorPhoneDesignWidth.roundToPx(),
                    height = SeniorPhoneDesignHeight.roundToPx(),
                )
            )
            val scale = constraints.maxWidth.toFloat() / designPlaceable.width

            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight,
            ) {
                designPlaceable.placeWithLayer(0, 0) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
            }
        }
    }
}

private val SeniorPhoneDesignWidth = 360.dp
private val SeniorPhoneDesignHeight = 960.dp
private val SeniorPhoneFeatureCardShape = RoundedCornerShape(12.dp)
private val SeniorPhoneGridCardShape = RoundedCornerShape(16.dp)

internal enum class SeniorSchedulePreviewState {
    None,
    One,
    Multiple,
}

@Composable
private fun SeniorPhoneDesign(
    configuration: SeniorScreenConfiguration,
    scheduleState: SeniorSchedulePreviewState,
) {
    val musicButton = configuration.buttons.firstOrNull {
        it.isMusicButton()
    }
    val hasMusic = musicButton != null
    val gridButtons = configuration.buttons
        .filterNot { button ->
            button.isMusicButton() ||
                button == SeniorHomeButtonType.Schedule
        }
        .toEmergencyLastGridSlots()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(SeniorOnColors.White)
            .drawWithCache {
                val gradientCenter = Offset(
                    x = size.width * 0.5f,
                    y = size.height * 0.58f,
                )
                val gradientBrush = Brush.radialGradient(
                    colors = listOf(
                        SeniorOnColors.Primary400,
                        Color(0xFFFAFFEC),
                    ),
                    center = gradientCenter,
                    radius = size.width * 0.78f,
                )
                onDrawBehind {
                    drawRect(brush = gradientBrush)
                }
            }
            .border(
                width = 5.dp,
                color = Color(0xFFCEC9D3),
                shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 16.dp),
    ) {
        SeniorPhoneStatusBar()
        SeniorPhoneWeatherHeader()

        Spacer(modifier = Modifier.height(16.dp))

        if (hasMusic) {
            SeniorMusicCard()

            Spacer(modifier = Modifier.height(12.dp))
        }

        SeniorScheduleCard(
            hasMusic = hasMusic,
            state = scheduleState,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            gridButtons.chunked(2).forEach { rowButtons ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowButtons.forEach { button ->
                        if (button == null) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            SeniorHomeButton(
                                button = button,
                                customLabel =
                                    configuration.customButtonLabels[button],
                                textStyle =
                                    configuration.fontSize.seniorHomeButtonTextStyle,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (rowButtons.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeniorPhoneStatusBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "9:30",
            modifier = Modifier.align(Alignment.CenterStart),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray800,
        )

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D1B20))
        )

        SeniorPhoneStatusIcons(modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun SeniorPhoneWeatherHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
    ) {
        Text(
            text = "5월 20일 (목)",
            modifier = Modifier.align(Alignment.TopStart),
            style = SeniorOnTextStyles.HeadingM,
            color = SeniorOnColors.Gray700,
            maxLines = 1,
        )

        Text(
            text = "오전 10:30",
            modifier = Modifier.align(Alignment.BottomStart),
            style = SeniorOnTextStyles.HeadingXXXL,
            color = SeniorOnColors.Gray800,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_weather_sun),
                contentDescription = "맑음",
                modifier = Modifier.size(34.dp),
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "20°C",
                    style = SeniorOnTextStyles.WeatherTemperature,
                    color = SeniorOnColors.Gray800,
                    maxLines = 1,
                )
                Text(
                    text = "맑음",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray700,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SeniorMusicCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .seniorPhoneCardShadow(SeniorPhoneFeatureCardShape)
            .clip(SeniorPhoneFeatureCardShape)
            .background(SeniorOnColors.Primary600)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "노래 듣기",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.White,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
            Text(
                text = "음악 앱으로 이동해요",
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.SupportWhite80,
                maxLines = 1,
            )
        }

        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_play),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.White,
            )
        }
    }
}

@Composable
private fun SeniorScheduleCard(
    hasMusic: Boolean,
    state: SeniorSchedulePreviewState,
) {
    val isFeatured = !hasMusic
    val backgroundColor = if (isFeatured) {
        SeniorOnColors.Primary600
    } else {
        SeniorOnColors.White
    }
    val contentColor = when {
        isFeatured && state == SeniorSchedulePreviewState.None ->
            SeniorOnColors.SupportWhite80
        isFeatured -> SeniorOnColors.White
        state == SeniorSchedulePreviewState.None -> SeniorOnColors.Gray300
        else -> SeniorOnColors.Schedule
    }
    val scheduleIcon = if (hasMusic) {
        R.drawable.ic_property1_calendar
    } else {
        R.drawable.ic_schedule
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .seniorPhoneCardShadow(SeniorPhoneFeatureCardShape)
            .clip(SeniorPhoneFeatureCardShape)
            .background(backgroundColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = scheduleIcon),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(10.dp))

        when (state) {
            SeniorSchedulePreviewState.None -> Text(
                text = "오늘은 일정이 없어요",
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.HeadingL,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )

            SeniorSchedulePreviewState.One -> Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "오후 3:00",
                    style = SeniorOnTextStyles.HeadingL,
                    color = if (isFeatured) SeniorOnColors.White else SeniorOnColors.Gray800,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Text(
                    text = "연세세브란스병원",
                    style = SeniorOnTextStyles.HeadingXS,
                    color = if (isFeatured) {
                        SeniorOnColors.SupportWhite80
                    } else {
                        SeniorOnColors.Gray600
                    },
                    maxLines = 1,
                )
            }

            SeniorSchedulePreviewState.Multiple -> {
                Text(
                    text = "오늘 일정 3개 있어요",
                    modifier = Modifier.weight(1f),
                    style = SeniorOnTextStyles.HeadingL,
                    color = if (isFeatured) SeniorOnColors.White else SeniorOnColors.Gray800,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_sm_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = if (isFeatured) SeniorOnColors.White else SeniorOnColors.Gray700,
                )
            }
        }
    }
}

@Composable
private fun SeniorHomeButton(
    button: SeniorHomeButtonType,
    customLabel: String?,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val isEmergency = button == SeniorHomeButtonType.Emergency

    Box(
        modifier = modifier
            .height(104.dp)
            .seniorPhoneCardShadow(SeniorPhoneGridCardShape)
            .clip(SeniorPhoneGridCardShape)
            .background(
                if (isEmergency) {
                    SeniorOnColors.Red300
                } else {
                    SeniorOnColors.White
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = customLabel ?: button.displayLabel(),
            style = textStyle,
            color = if (isEmergency) SeniorOnColors.White else SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Clip,
        )
    }
}

private fun Modifier.seniorPhoneCardShadow(
    shape: RoundedCornerShape,
): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = 11.dp,
        spread = 0.dp,
        color = SeniorOnColors.Black.copy(alpha = 0.08f),
        offset = DpOffset.Zero,
    ),
)

private fun List<SeniorHomeButtonType>.toEmergencyLastGridSlots():
    List<SeniorHomeButtonType?> {
    val uniqueButtons = distinct()
    val regularButtons = uniqueButtons.filterNot {
        it == SeniorHomeButtonType.Emergency
    }
    val emergencyButton = uniqueButtons.firstOrNull {
        it == SeniorHomeButtonType.Emergency
    } ?: return regularButtons

    return buildList {
        addAll(regularButtons)
        if (regularButtons.size % 2 == 0) {
            add(null)
        }
        add(emergencyButton)
    }
}

@Composable
private fun SeniorPhoneStatusIcons(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, size.height * 0.35f)
                quadraticTo(size.width / 2f, 0f, size.width, size.height * 0.35f)
                close()
            }
            drawPath(path, color = SeniorOnColors.Gray200)
        }

        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color = SeniorOnColors.Gray800)
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_sm_battery),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Gray700,
        )
    }
}

internal val SeniorFontSize.seniorHomeButtonTextStyle: TextStyle
    get() = when (this) {
        SeniorFontSize.Large -> SeniorOnTextStyles.HeadingXXXL
        SeniorFontSize.Normal -> SeniorOnTextStyles.HeadingXXL
        SeniorFontSize.Small -> SeniorOnTextStyles.HeadingXL
    }

@Preview(
    name = "Music - One schedule",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneMusicOneSchedulePreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = true,
        scheduleState = SeniorSchedulePreviewState.One,
    )
}

@Preview(
    name = "Music - No schedule",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneMusicNoSchedulePreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = true,
        scheduleState = SeniorSchedulePreviewState.None,
    )
}

@Preview(
    name = "Music - Multiple schedules",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneMusicMultipleSchedulesPreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = true,
        scheduleState = SeniorSchedulePreviewState.Multiple,
    )
}

@Preview(
    name = "No music - One schedule",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneNoMusicOneSchedulePreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = false,
        scheduleState = SeniorSchedulePreviewState.One,
    )
}

@Preview(
    name = "No music - No schedule",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneNoMusicNoSchedulePreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = false,
        scheduleState = SeniorSchedulePreviewState.None,
    )
}

@Preview(
    name = "No music - Multiple schedules",
    group = "Senior phone states",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhoneNoMusicMultipleSchedulesPreview() {
    SeniorPhoneScenarioPreview(
        hasMusic = false,
        scheduleState = SeniorSchedulePreviewState.Multiple,
    )
}

@Composable
private fun SeniorPhoneScenarioPreview(
    hasMusic: Boolean,
    scheduleState: SeniorSchedulePreviewState,
) {
    val configuration = if (hasMusic) {
        MockDisplayFixtures.defaultScreenConfiguration
    } else {
        MockDisplayFixtures.defaultScreenConfiguration.copy(
            buttons = MockDisplayFixtures.defaultScreenConfiguration.buttons
                .filterNot { it.isMusicButton() },
        )
    }

    SENIOR_ONTheme {
        SeniorPhonePreview(
            configuration = configuration,
            previewWidth = SeniorPhoneDesignWidth,
            previewHeight = SeniorPhoneDesignHeight,
            scheduleState = scheduleState,
        )
    }
}
