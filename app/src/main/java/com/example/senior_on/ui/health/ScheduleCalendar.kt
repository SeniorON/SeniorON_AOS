package com.example.senior_on.ui.health

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.YearMonth

internal enum class ScheduleCalendarMode {
    Hospital,
    BottomSheet
}

@Composable
internal fun ScheduleCalendar(
    displayedMonth: YearMonth,
    selectedDay: Int?,
    markedDays: Set<Int> = emptySet(),
    onDayClick: (Int) -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    headerEndContent: (@Composable () -> Unit)? = null,
    mode: ScheduleCalendarMode = ScheduleCalendarMode.Hospital,
    modifier: Modifier = Modifier
) {
    val layout = when (mode) {
        ScheduleCalendarMode.Hospital -> HospitalCalendarLayout
        ScheduleCalendarMode.BottomSheet -> BottomSheetCalendarLayout
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarMonthHeader(
            displayedMonth = displayedMonth,
            onPreviousMonthClick = onPreviousMonthClick,
            onNextMonthClick = onNextMonthClick,
            headerEndContent = headerEndContent,
            layout = layout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(layout.headerBottomSpacing))

        CalendarWeekdayHeader(layout = layout)

        Spacer(modifier = Modifier.height(layout.weekdayBottomSpacing))

        AnimatedContent(
            targetState = displayedMonth,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 300),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetX = { fullWidth -> -fullWidth }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 300),
                        initialOffsetX = { fullWidth -> -fullWidth }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(durationMillis = 300),
                        targetOffsetX = { fullWidth -> fullWidth }
                    )
                }
            },
            label = "scheduleCalendarMonthTransition"
        ) { animatedMonth ->
            CalendarMonthDays(
                displayedMonth = animatedMonth,
                selectedDay = selectedDay,
                markedDays = markedDays,
                onDayClick = onDayClick,
                layout = layout
            )
        }
    }
}

@Composable
private fun CalendarMonthHeader(
    displayedMonth: YearMonth,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    headerEndContent: (@Composable () -> Unit)?,
    layout: ScheduleCalendarLayoutValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sm_arrow_left),
                contentDescription = "이전 달",
                tint = SeniorOnColors.Gray800,
                modifier = Modifier
                    .size(layout.headerIconSize)
                    .clickable(onClick = onPreviousMonthClick)
            )
            AnimatedContent(
                targetState = displayedMonth,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 300),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(durationMillis = 300),
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    }
                },
                label = "scheduleCalendarHeaderTransition"
            ) { animatedMonth ->
                Text(
                    text = "${animatedMonth.year}. ${animatedMonth.monthValue}월",
                    style = SeniorOnTextStyles.BodyLSemiBold,
                    color = SeniorOnColors.Gray800,
                    modifier = Modifier.padding(horizontal = layout.headerTitleHorizontalPadding)
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_sm_arrow_right),
                contentDescription = "다음 달",
                tint = SeniorOnColors.Gray800,
                modifier = Modifier
                    .size(layout.headerIconSize)
                    .clickable(onClick = onNextMonthClick)
            )
        }

        headerEndContent?.let { content ->
            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CalendarWeekdayHeader(
    layout: ScheduleCalendarLayoutValues
) {
    val weekdayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(layout.cellHorizontalSpacing)
    ) {
        weekdayLabels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(layout.cellHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = when (index) {
                        0 -> SeniorOnColors.Red300
                        6 -> SeniorOnColors.SupportBlue
                        else -> SeniorOnColors.Gray500
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthDays(
    displayedMonth: YearMonth,
    selectedDay: Int?,
    markedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
    layout: ScheduleCalendarLayoutValues
) {
    val firstDayOffset = displayedMonth.atDay(1).dayOfWeek.value % 7
    val cells = buildList<Int?> {
        repeat(firstDayOffset) { add(null) }
        repeat(displayedMonth.lengthOfMonth()) { add(it + 1) }
        while (size % 7 != 0) add(null)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(layout.cellVerticalSpacing)
    ) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(layout.cellHorizontalSpacing)
            ) {
                week.forEachIndexed { weekdayIndex, day ->
                    CalendarDay(
                        day = day,
                        dayColor = when (weekdayIndex) {
                            0 -> SeniorOnColors.Red300
                            6 -> SeniorOnColors.SupportBlue
                            else -> SeniorOnColors.Gray500
                        },
                        selected = day == selectedDay,
                        marked = day in markedDays,
                        onClick = { day?.let(onDayClick) },
                        layout = layout,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int?,
    dayColor: Color,
    selected: Boolean,
    marked: Boolean,
    onClick: () -> Unit,
    layout: ScheduleCalendarLayoutValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(layout.cellHeight),
        contentAlignment = Alignment.Center
    ) {
        if (day == null) return@Box

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(layout.dayCornerRadius))
                .background(if (selected) SeniorOnColors.Primary600 else Color.Transparent)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = SeniorOnTextStyles.BodySMedium,
                color = when {
                    selected -> SeniorOnColors.SupportWhite100
                    else -> dayColor
                }
            )
        }

        if (marked && !selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = layout.markedDotTopPadding,
                        end = layout.markedDotEndPadding
                    )
                    .size(layout.markedDotSize)
                    .clip(CircleShape)
                    .background(SeniorOnColors.Primary600)
            )
        }
    }
}

@Preview(
    name = "일정 캘린더 - Hospital",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
private fun HospitalScheduleCalendarPreview() {
    SENIOR_ONTheme {
        ScheduleCalendar(
            displayedMonth = YearMonth.of(2026, 6),
            selectedDay = 17,
            markedDays = setOf(19, 22, 26),
            onDayClick = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            mode = ScheduleCalendarMode.Hospital,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(
    name = "일정 캘린더 - BottomSheet",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360
)
@Composable
private fun BottomSheetScheduleCalendarPreview() {
    SENIOR_ONTheme {
        ScheduleCalendar(
            displayedMonth = YearMonth.of(2026, 6),
            selectedDay = 17,
            markedDays = setOf(19, 22, 26),
            onDayClick = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            headerEndContent = {
                Text(
                    text = "닫기",
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600,
                    modifier = Modifier.padding(10.dp)
                )
            },
            mode = ScheduleCalendarMode.BottomSheet,
            modifier = Modifier.padding(top = 24.5.dp, bottom = 19.dp, start = 16.dp, end = 16.dp)
        )
    }
}
