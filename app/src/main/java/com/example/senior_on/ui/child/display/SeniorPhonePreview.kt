package com.example.senior_on.ui.child.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.domain.model.display.DisplayTodaySchedule
import com.example.senior_on.domain.model.display.DisplayWeather
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.ui.parent.home.SeniorHomeContent
import com.example.senior_on.ui.parent.home.rememberKoreaDateTime
import com.example.senior_on.ui.parent.home.seniorHomeBackground
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeButtonUiModel
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeScheduleUiState
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeWeatherUiState
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun SeniorPhonePreview(
    configuration: SeniorScreenConfiguration,
    previewWidth: Dp = 116.dp,
    previewHeight: Dp = 263.dp,
    weather: DisplayWeather? = null,
    isWeatherLoading: Boolean = false,
    todaySchedule: DisplayTodaySchedule? = null,
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
                    weather = weather,
                    isWeatherLoading = isWeatherLoading,
                    todaySchedule = todaySchedule,
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

@Composable
private fun SeniorPhoneDesign(
    configuration: SeniorScreenConfiguration,
    weather: DisplayWeather?,
    isWeatherLoading: Boolean,
    todaySchedule: DisplayTodaySchedule?,
) {
    val now = rememberKoreaDateTime()
    val musicButtonType = configuration.buttons.firstOrNull {
        it.isMusicButton()
    }
    val musicButton = musicButtonType?.toPreviewButton(
        id = -1L,
        label = configuration.customButtonLabels[musicButtonType]
            ?: musicButtonType.displayLabel(),
    )
    val buttons = configuration.buttons
        .filterNot { it.isMusicButton() }
        .mapIndexed { index, type ->
            type.toPreviewButton(
                id = index.toLong(),
                label = configuration.customButtonLabels[type]
                    ?: type.displayLabel(),
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .seniorHomeBackground()
            .padding(horizontal = 16.dp),
    ) {
        SeniorPhoneStatusBar(now)

        SeniorHomeContent(
            now = now,
            configuration = configuration,
            musicButton = musicButton,
            buttons = buttons,
            scheduleUiState = todaySchedule.toPreviewScheduleUiState(),
            weatherUiState = weather.toPreviewWeatherUiState(isWeatherLoading),
            interactionEnabled = false,
            onMusicClick = {},
            onScheduleClick = {},
            onButtonClick = {},
        )
    }
}

@Composable
private fun SeniorPhoneStatusBar(
    now: LocalDateTime,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = now.format(StatusBarTimeFormatter),
            modifier = Modifier.align(Alignment.CenterStart),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray800,
        )

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF1D1B20)),
        )

        SeniorPhoneStatusIcons(modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun SeniorPhoneStatusIcons(
    modifier: Modifier = Modifier,
) {
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

private fun SeniorHomeButtonType.toPreviewButton(
    id: Long,
    label: String,
) = ParentHomeButtonUiModel(
    id = id,
    type = this,
    label = label,
    actionType = null,
    actionValue = name,
    packageName = null,
)

private fun DisplayTodaySchedule?.toPreviewScheduleUiState() =
    ParentHomeScheduleUiState(
        count = this?.count ?: 0,
        title = this?.title,
        description = this?.description,
        scheduledTime = this?.scheduledTime.toLocalTimeOrNull(),
        isLoading = false,
    )

private fun DisplayWeather?.toPreviewWeatherUiState(
    isLoading: Boolean,
): ParentHomeWeatherUiState = when {
    isLoading -> ParentHomeWeatherUiState(text = "날씨 확인 중")
    this == null -> ParentHomeWeatherUiState(text = "날씨 정보 없음")
    else -> ParentHomeWeatherUiState(
        temperature = temperatureCelsius,
        status = status,
        text = description
            ?.takeIf(String::isNotBlank)
            ?: status.toKoreanWeatherLabel()
            ?: "날씨 정보 없음",
    )
}

private fun String?.toLocalTimeOrNull(): LocalTime? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null

    return runCatching { LocalTime.parse(value) }.getOrNull()
        ?: ScheduleTimePattern.find(value)
            ?.let { result ->
                val hour = result.groupValues[1].toIntOrNull() ?: return@let null
                val minute = result.groupValues[2].toIntOrNull() ?: return@let null
                runCatching { LocalTime.of(hour, minute) }.getOrNull()
            }
}

private fun String?.toKoreanWeatherLabel(): String? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val normalized = value.uppercase()
    return when {
        "CLEAR" in normalized || "SUNNY" in normalized -> "맑음"
        "CLOUD" in normalized -> "흐림"
        "RAIN" in normalized -> "비"
        "SNOW" in normalized -> "눈"
        "FOG" in normalized || "MIST" in normalized -> "안개"
        else -> value
    }
}

private val SeniorPhoneDesignWidth = 360.dp
private val SeniorPhoneDesignHeight = 960.dp
private val StatusBarTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm", Locale.KOREAN)
private val ScheduleTimePattern =
    Regex("""(?:^|[T\s])(\d{1,2}):(\d{2})""")

@Preview(
    name = "Shared senior home preview",
    showBackground = true,
    widthDp = 360,
    heightDp = 960,
)
@Composable
private fun SeniorPhonePreviewPreview() {
    SENIOR_ONTheme {
        SeniorPhonePreview(
            configuration = MockDisplayFixtures.defaultScreenConfiguration,
            previewWidth = 180.dp,
            previewHeight = 408.dp,
            weather = DisplayWeather(
                temperatureCelsius = 20,
                status = "CLEAR",
                description = "맑음",
                observedAt = null,
            ),
            todaySchedule = DisplayTodaySchedule(
                title = "연세세브란스병원",
                description = null,
                count = 1,
                displayType = null,
                id = 1L,
                scheduledTime = "15:00",
            ),
        )
    }
}
