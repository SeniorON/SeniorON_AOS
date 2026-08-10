package com.example.senior_on.ui.parent.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.source.mock.fixtures.MockDisplayFixtures
import com.example.senior_on.domain.model.display.SeniorFontSize
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import com.example.senior_on.ui.child.display.displayLabel
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeButtonUiModel
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeScheduleUiState
import com.example.senior_on.ui.parent.home.viewmodel.ParentHomeWeatherUiState
import com.example.senior_on.ui.parent.schedule.viewmodel.toParentDisplayTime
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ParentHomeScreen(
    configuration: SeniorScreenConfiguration,
    musicButton: ParentHomeButtonUiModel?,
    buttons: List<ParentHomeButtonUiModel>,
    scheduleUiState: ParentHomeScheduleUiState,
    weatherUiState: ParentHomeWeatherUiState,
    onMusicClick: (ParentHomeButtonUiModel) -> Unit,
    onScheduleClick: () -> Unit,
    onButtonClick: (ParentHomeButtonUiModel) -> Unit,
    onChangeDefaultHomeClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val now = rememberKoreaDateTime()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .seniorHomeBackground()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 24.dp,
                ),
        ) {
            SeniorHomeContent(
                now = now,
                configuration = configuration,
                musicButton = musicButton,
                buttons = buttons,
                scheduleUiState = scheduleUiState,
                weatherUiState = weatherUiState,
                onMusicClick = onMusicClick,
                onScheduleClick = onScheduleClick,
                onButtonClick = onButtonClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onChangeDefaultHomeClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "기본 홈 앱 되돌리기 (테스트)",
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray700,
                )
            }
        }
    }
}

@Composable
internal fun ColumnScope.SeniorHomeContent(
    now: LocalDateTime,
    configuration: SeniorScreenConfiguration,
    musicButton: ParentHomeButtonUiModel?,
    buttons: List<ParentHomeButtonUiModel>,
    scheduleUiState: ParentHomeScheduleUiState,
    weatherUiState: ParentHomeWeatherUiState,
    onMusicClick: (ParentHomeButtonUiModel) -> Unit,
    onScheduleClick: () -> Unit,
    onButtonClick: (ParentHomeButtonUiModel) -> Unit,
    interactionEnabled: Boolean = true,
) {
    val gridButtons = buttons
        .filterNot { it.type == SeniorHomeButtonType.Schedule }
        .withEmergencyAtFixedGridSlot()

    ParentDateWeatherHeader(
        now = now,
        weatherUiState = weatherUiState,
    )

    Spacer(modifier = Modifier.height(16.dp))

    musicButton?.let { button ->
        ParentMusicCard(
            enabled = interactionEnabled,
            onClick = { onMusicClick(button) },
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    ParentTodayScheduleCard(
        uiState = scheduleUiState,
        isFeatured = musicButton == null,
        interactionEnabled = interactionEnabled,
        onClick = onScheduleClick,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        gridButtons.chunked(2).forEach { rowButtons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowButtons.forEach { button ->
                    ParentHomeGridButton(
                        button = button,
                        textStyle = configuration.fontSize.homeButtonTextStyle,
                        interactionEnabled = interactionEnabled,
                        onClick = { onButtonClick(button) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowButtons.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

    }
}

@Composable
private fun ParentDateWeatherHeader(
    now: LocalDateTime,
    weatherUiState: ParentHomeWeatherUiState,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = now.format(
                    DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
                ),
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray700,
                maxLines = 1,
            )
            Text(
                text = now.format(
                    DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
                ),
                style = SeniorOnTextStyles.Display,
                color = SeniorOnColors.Gray800,
                maxLines = 1,
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_weather_sun),
                contentDescription = weatherUiState.text,
                modifier = Modifier.size(34.dp).align(Alignment.Top),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = weatherUiState.temperature
                        ?.let { "$it°C" }
                        ?: "--°C",
                    style = SeniorOnTextStyles.HeadingM,
                    color = SeniorOnColors.Gray800,
                )
                Text(
                    text = weatherUiState.text,
                    style = SeniorOnTextStyles.HeadingS,
                    color = SeniorOnColors.Gray700,
                )
            }
        }
    }
}

@Composable
private fun ParentMusicCard(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .dropShadow(
                shape = ParentFeatureCardShape,
                shadow = Shadow(
                    radius = 12.dp,
                    color = Color(0xB2B1C595),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                ),
            )
            .clip(ParentFeatureCardShape)
            .background(SeniorOnColors.Primary700)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "노래 듣기",
                style = SeniorOnTextStyles.HeadingL,
                color = SeniorOnColors.SupportWhite100,
                maxLines = 1,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_big_play),
            contentDescription = "노래 듣기 실행",
            modifier = Modifier.size(42.dp),
            tint = SeniorOnColors.White,
        )
    }
}

@Composable
private fun ParentTodayScheduleCard(
    uiState: ParentHomeScheduleUiState,
    isFeatured: Boolean,
    interactionEnabled: Boolean,
    onClick: () -> Unit,
) {
    val scheduleCount = uiState.count
    val backgroundColor = if (isFeatured) {
        SeniorOnColors.Primary700
    } else {
        SeniorOnColors.White
    }
    val primaryContentColor = if (isFeatured) {
        SeniorOnColors.SupportWhite100
    } else {
        SeniorOnColors.Gray800
    }
    val secondaryContentColor = if (isFeatured) {
        SeniorOnColors.SupportWhite80
    } else {
        SeniorOnColors.Gray600
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .dropShadow(
                shape = ParentFeatureCardShape,
                shadow = Shadow(
                    radius = 11.dp,
                    color = Color(0x14000000),
                    offset = DpOffset.Zero,
                ),
            )
            .clip(ParentFeatureCardShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isFeatured) Color.Transparent else SeniorOnColors.White,
                shape = ParentFeatureCardShape,
            )
            .clickable(
                enabled = interactionEnabled && !uiState.isLoading && scheduleCount > 0,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = if (isFeatured) {
                    SeniorOnColors.SupportWhite100
                } else {
                    SeniorOnColors.Schedule
                },
                strokeWidth = 3.dp,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_big_schedule),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = if (isFeatured && scheduleCount == 0) {
                    SeniorOnColors.SupportWhite80
                } else if (isFeatured) {
                    SeniorOnColors.SupportWhite100
                } else if (scheduleCount == 0) {
                    SeniorOnColors.Gray300
                } else {
                    SeniorOnColors.Schedule
                },
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        when {
            uiState.isLoading -> Text(
                text = "오늘 일정을 불러오고 있어요",
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.HeadingL,
                color = if (isFeatured) {
                    SeniorOnColors.SupportWhite80
                } else {
                    SeniorOnColors.Gray300
                },
            )

            scheduleCount == 0 -> Text(
                text = "오늘은 일정이 없어요",
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.HeadingL,
                color = if (isFeatured) {
                    SeniorOnColors.SupportWhite80
                } else {
                    SeniorOnColors.Gray300
                },
                maxLines = 1,
            )

            scheduleCount == 1 -> Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = uiState.scheduledTime?.toParentDisplayTime()
                        ?: "오늘 일정이 있어요",
                    style = SeniorOnTextStyles.HeadingL,
                    color = primaryContentColor,
                    maxLines = 1,
                )
                Text(
                    text = listOfNotNull(
                        uiState.title?.takeIf(String::isNotBlank),
                        uiState.description?.takeIf(String::isNotBlank),
                    ).joinToString(" · "),
                    style = SeniorOnTextStyles.HeadingXS,
                    color = secondaryContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            else -> {
                Text(
                    text = "오늘 일정 ${scheduleCount}개 있어요",
                    modifier = Modifier.weight(1f),
                    style = SeniorOnTextStyles.HeadingL,
                    color = primaryContentColor,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.width(11.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_sm_arrow_right),
                    contentDescription = "일정 목록 보기",
                    modifier = Modifier.size(34.dp),
                    tint = if (isFeatured) {
                        SeniorOnColors.SupportWhite100
                    } else {
                        SeniorOnColors.Gray700
                    },
                )
            }
        }
    }
}

@Composable
private fun ParentHomeGridButton(
    button: ParentHomeButtonUiModel,
    textStyle: TextStyle,
    interactionEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmergency = button.type == SeniorHomeButtonType.Emergency

    Box(
        modifier = modifier
            .height(104.dp)
            .dropShadow(
                shape = ParentGridCardShape,
                shadow = Shadow(
                    radius = 11.dp,
                    color = Color(0x14000000),
                    offset = DpOffset.Zero,
                ),
            )
            .clip(ParentGridCardShape)
            .background(
                if (isEmergency) SeniorOnColors.Red400 else SeniorOnColors.White
            )
            .clickable(enabled = interactionEnabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = button.label,
            modifier = Modifier.padding(horizontal = 20.dp),
            style = textStyle,
            color = if (isEmergency) SeniorOnColors.White else SeniorOnColors.Gray800,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal val SeniorFontSize.homeButtonTextStyle: TextStyle
    get() = when (this) {
        SeniorFontSize.Large -> SeniorOnTextStyles.HeadingXXXL
        SeniorFontSize.Normal -> SeniorOnTextStyles.HeadingXL
        SeniorFontSize.Small -> SeniorOnTextStyles.HeadingL
    }

internal fun Modifier.seniorHomeBackground(): Modifier =
    background(SeniorOnColors.White)
        .drawWithCache {
            val gradient = Brush.radialGradient(
                colors = listOf(
                    SeniorOnColors.Primary400.copy(alpha = 0.2f),
                    Color(0xFFFAFFEC).copy(alpha = 0.2f),
                ),
                center = Offset(
                    x = size.width * 0.5551f,
                    y = size.height * 0.5812f,
                ),
                radius = size.height * 0.5869f,
            )
            onDrawBehind { drawRect(gradient) }
        }

@Composable
internal fun rememberKoreaDateTime(): LocalDateTime {
    val now by produceState(initialValue = LocalDateTime.now(KoreaZoneId)) {
        while (true) {
            value = LocalDateTime.now(KoreaZoneId)
            delay(30_000)
        }
    }
    return now
}

private val ParentFeatureCardShape =
    RoundedCornerShape(SeniorOnRadius.Large)
private val ParentGridCardShape =
    RoundedCornerShape(20.dp)
private val KoreaZoneId = ZoneId.of("Asia/Seoul")

private fun List<ParentHomeButtonUiModel>.withEmergencyAtFixedGridSlot():
    List<ParentHomeButtonUiModel> {
    val emergency = firstOrNull { it.type == SeniorHomeButtonType.Emergency }
    val otherButtons = filterNot { it.type == SeniorHomeButtonType.Emergency }
    if (emergency == null) return otherButtons

    return buildList {
        addAll(otherButtons.take(7))
        add(emergency)
        addAll(otherButtons.drop(7))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 960)
@Composable
private fun ParentHomeScreenPreview() {
    SENIOR_ONTheme {
        ParentHomeScreen(
            configuration = MockDisplayFixtures.defaultScreenConfiguration,
            musicButton = ParentHomeButtonUiModel(
                id = 0,
                type = SeniorHomeButtonType.Melon,
                label = "노래 듣기",
                actionType = "APP",
                actionValue = "MELON",
                packageName = "com.iloen.melon",
            ),
            buttons = MockDisplayFixtures.defaultScreenConfiguration.buttons
                .filterNot { it == SeniorHomeButtonType.Melon }
                .mapIndexed { index, type ->
                    ParentHomeButtonUiModel(
                        id = index.toLong(),
                        type = type,
                        label = type.displayLabel(),
                        actionType = "DEFAULT",
                        actionValue = type.name,
                        packageName = null,
                    )
                },
            scheduleUiState = ParentHomeScheduleUiState(isLoading = false),
            weatherUiState = ParentHomeWeatherUiState(
                temperature = 20,
                status = "CLEAR",
                text = "맑음",
            ),
            onMusicClick = {},
            onScheduleClick = {},
            onButtonClick = {},
            onChangeDefaultHomeClick = {},
        )
    }
}
