package com.example.senior_on.ui.health

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class ScheduleCalendarLayoutValues(
    val headerBottomSpacing: Dp,
    val headerIconSize: Dp,
    val headerTitleHorizontalPadding: Dp,
    val cellHeight: Dp,
    val cellHorizontalSpacing: Dp,
    val cellVerticalSpacing: Dp,
    val weekdayBottomSpacing: Dp,
    val dayCornerRadius: Dp,
    val markedDotTopPadding: Dp,
    val markedDotEndPadding: Dp,
    val markedDotSize: Dp
)

internal val HospitalCalendarLayout = ScheduleCalendarLayoutValues(
    headerBottomSpacing = 12.dp,
    headerIconSize = 18.dp,
    headerTitleHorizontalPadding = 16.dp,
    cellHeight = 32.dp,
    cellHorizontalSpacing = 1.dp,
    cellVerticalSpacing = 4.dp,
    weekdayBottomSpacing = 4.dp,
    dayCornerRadius = 8.dp,
    markedDotTopPadding = 4.dp,
    markedDotEndPadding = 4.5.dp,
    markedDotSize = 4.dp
)

internal val BottomSheetCalendarLayout = ScheduleCalendarLayoutValues(
    headerBottomSpacing = 18.5.dp,
    headerIconSize = 18.dp,
    headerTitleHorizontalPadding = 16.dp,
    cellHeight = 32.dp,
    cellHorizontalSpacing = 8.dp,
    cellVerticalSpacing = 4.dp,
    weekdayBottomSpacing = 4.dp,
    dayCornerRadius = 8.dp,
    markedDotTopPadding = 4.dp,
    markedDotEndPadding = 4.5.dp,
    markedDotSize = 4.dp
)
