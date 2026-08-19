package com.example.senior_on.ui.child.health

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.senior_on.R
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.YearMonth

private val CycleStepperCircleSize = 24.dp
private val CycleStepperIconSize = 10.dp
private val CycleStepperIconStroke = 1.5.dp
private val CycleStepperIconGap = 10.dp

private val PeriodStepperCircleSize = 20.dp
private val PeriodStepperIconSize = 8.333.dp
private val PeriodStepperIconStroke = 1.667.dp
private val PeriodStepperIconGap = 10.dp

enum class MedicationRepeatFrequency(val label: String) {
    Daily("매일"),
    Weekly("매주")
}

enum class MedicationRepeatDuration(val label: String) {
    Continuous("계속 복용"),
    Period("복용 기간"),
    Date("날짜 지정")
}

data class MedicationRepeatSelection(
    val frequency: MedicationRepeatFrequency = MedicationRepeatFrequency.Daily,
    val cycleValue: Int = 1,
    val weekdays: Set<Int> = emptySet(),
    val duration: MedicationRepeatDuration = MedicationRepeatDuration.Continuous,
    val periodValue: Int = 3,
    val endDate: LocalDate? = null
) {
    fun summaryLabel(startDate: LocalDate?): String =
        when (duration) {
            MedicationRepeatDuration.Continuous -> "계속 복용"
            MedicationRepeatDuration.Period -> {
                val end = (startDate ?: koreaToday())
                    .plusWeeks(periodValue.toLong())
                "${periodValue}주 · ${end.monthValue}월${end.dayOfMonth}일까지"
            }
            MedicationRepeatDuration.Date -> {
                val end = endDate ?: startDate
                if (end != null) {
                    "${end.monthValue}월${end.dayOfMonth}일까지"
                } else {
                    "날짜 지정"
                }
            }
        }
}

@Composable
internal fun MedicationRepeatBottomSheet(
    initial: MedicationRepeatSelection,
    onCancel: () -> Unit,
    onConfirm: (MedicationRepeatSelection) -> Unit
) {
    NonDraggableBottomSheet {
        MedicationRepeatSheetContent(
            initial = initial,
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun MedicationRepeatSheetContent(
    initial: MedicationRepeatSelection,
    onCancel: () -> Unit,
    onConfirm: (MedicationRepeatSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    var frequency by remember(initial) { mutableStateOf(initial.frequency) }
    var cycleValue by remember(initial) { mutableIntStateOf(initial.cycleValue.coerceAtLeast(1)) }
    var weekdays by remember(initial) { mutableStateOf(initial.weekdays) }
    var duration by remember(initial) { mutableStateOf(initial.duration) }
    var periodValue by remember(initial) { mutableIntStateOf(initial.periodValue.coerceAtLeast(1)) }
    var endDate by remember(initial) { mutableStateOf(initial.endDate) }
    var showEndDateCalendar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "반복",
            style = SeniorOnTextStyles.BodyLMedium,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(18.dp))

        MedicationRepeatFrequencySelector(
            selected = frequency,
            onSelected = { selected ->
                frequency = selected
                if (selected == MedicationRepeatFrequency.Daily) {
                    cycleValue = 1
                }
            }
        )

        AnimatedVisibility(
            visible = frequency == MedicationRepeatFrequency.Weekly,
            enter = fadeIn(tween(220)) + expandVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(tween(160)) + shrinkVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "반복 주기",
                        style = SeniorOnTextStyles.BodyMSemiBold,
                        color = SeniorOnColors.Gray800,
                        modifier = Modifier.weight(1f)
                    )
                    MedicationRepeatStepper(
                        label = cycleLabel(MedicationRepeatFrequency.Weekly, cycleValue),
                        onDecrease = { if (cycleValue > 1) cycleValue -= 1 },
                        onIncrease = { cycleValue += 1 },
                        circleSize = CycleStepperCircleSize,
                        iconSize = CycleStepperIconSize,
                        iconStroke = CycleStepperIconStroke,
                        iconGap = CycleStepperIconGap
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "무슨 요일에 드시나요?",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray600,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                MedicationRepeatWeekdaySelector(
                    selectedDays = weekdays,
                    onDayToggle = { dayIndex ->
                        weekdays = if (dayIndex in weekdays) {
                            weekdays - dayIndex
                        } else {
                            weekdays + dayIndex
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "언제까지 복용하시나요?",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray600,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        MedicationRepeatDurationOption(
            label = MedicationRepeatDuration.Continuous.label,
            selected = duration == MedicationRepeatDuration.Continuous,
            onClick = { duration = MedicationRepeatDuration.Continuous }
        )
        MedicationRepeatDurationOption(
            label = MedicationRepeatDuration.Period.label,
            selected = duration == MedicationRepeatDuration.Period,
            onClick = { duration = MedicationRepeatDuration.Period },
            trailing = {
                if (duration == MedicationRepeatDuration.Period) {
                    MedicationRepeatStepper(
                        label = "${periodValue}주",
                        onDecrease = { if (periodValue > 1) periodValue -= 1 },
                        onIncrease = { periodValue += 1 },
                        circleSize = PeriodStepperCircleSize,
                        iconSize = PeriodStepperIconSize,
                        iconStroke = PeriodStepperIconStroke,
                        iconGap = PeriodStepperIconGap
                    )
                }
            }
        )
        MedicationRepeatDurationOption(
            label = MedicationRepeatDuration.Date.label,
            selected = duration == MedicationRepeatDuration.Date,
            onClick = {
                duration = MedicationRepeatDuration.Date
                showEndDateCalendar = true
            },
            showDivider = false,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (duration == MedicationRepeatDuration.Date && endDate != null) {
                        Text(
                            text = "${endDate!!.monthValue}월 ${endDate!!.dayOfMonth}일",
                            style = SeniorOnTextStyles.BodyMMedium.copy(
                                lineHeight = 16.sp
                            ),
                            color = SeniorOnColors.Gray800,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar2),
                        contentDescription = null,
                        tint = SeniorOnColors.Gray400,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    duration = MedicationRepeatDuration.Date
                                    showEndDateCalendar = true
                                }
                            )
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicationRepeatSheetButton(
                label = "취소",
                backgroundColor = SeniorOnColors.SupportWhite100,
                contentColor = SeniorOnColors.Gray400,
                borderColor = SeniorOnColors.Gray200,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            MedicationRepeatSheetButton(
                label = "확인",
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.SupportWhite100,
                onClick = {
                    onConfirm(
                        MedicationRepeatSelection(
                            frequency = frequency,
                            cycleValue = cycleValue,
                            weekdays = when (frequency) {
                                MedicationRepeatFrequency.Daily ->
                                    MedicationWeekdayLabels.indices.toSet()
                                else -> weekdays
                            },
                            duration = duration,
                            periodValue = periodValue,
                            endDate = endDate
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showEndDateCalendar) {
        MedicationEndDateCalendarDialog(
            initialDate = endDate ?: koreaToday(),
            onCancel = { showEndDateCalendar = false },
            onConfirm = { selected ->
                endDate = selected
                duration = MedicationRepeatDuration.Date
                showEndDateCalendar = false
            }
        )
    }
}

@Composable
private fun MedicationEndDateCalendarDialog(
    initialDate: LocalDate,
    onCancel: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var displayedMonth by remember(initialDate) {
        mutableStateOf(YearMonth.from(initialDate))
    }
    var selectedDay by remember(initialDate) {
        mutableIntStateOf(initialDate.dayOfMonth)
    }
    val adjustedDay = selectedDay.coerceAtMost(displayedMonth.lengthOfMonth())

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .width(302.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SeniorOnColors.SupportWhite100)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            ScheduleCalendar(
                displayedMonth = displayedMonth,
                selectedDay = adjustedDay,
                onDayClick = { selectedDay = it },
                onPreviousMonthClick = {
                    displayedMonth = displayedMonth.minusMonths(1)
                },
                onNextMonthClick = {
                    displayedMonth = displayedMonth.plusMonths(1)
                },
                mode = ScheduleCalendarMode.BottomSheet,
                modifier = Modifier
                    .width(264.dp)
                    .align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MedicationRepeatSheetButton(
                    label = "취소",
                    backgroundColor = SeniorOnColors.Gray100,
                    contentColor = SeniorOnColors.Gray600,
                    onClick = onCancel,
                    modifier = Modifier.weight(91f)
                )
                MedicationRepeatSheetButton(
                    label = "확인",
                    backgroundColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.SupportWhite100,
                    onClick = {
                        onConfirm(displayedMonth.atDay(adjustedDay))
                    },
                    modifier = Modifier.weight(174f)
                )
            }
        }
    }
}

private fun cycleLabel(frequency: MedicationRepeatFrequency, value: Int): String =
    when (frequency) {
        MedicationRepeatFrequency.Daily -> "${value}일마다"
        MedicationRepeatFrequency.Weekly -> "${value}주마다"
    }

@Composable
private fun MedicationRepeatFrequencySelector(
    selected: MedicationRepeatFrequency,
    onSelected: (MedicationRepeatFrequency) -> Unit
) {
    val shape = RoundedCornerShape(69.dp)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(SeniorOnColors.Background3)
    ) {
        val tabWidth = maxWidth / MedicationRepeatFrequency.entries.size
        val indicatorOffset by animateDpAsState(
            targetValue = if (selected == MedicationRepeatFrequency.Daily) 0.dp else tabWidth,
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            label = "medicationRepeatIndicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(shape)
                .background(SeniorOnColors.Primary100)
                .border(1.dp, SeniorOnColors.Primary400, shape)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            MedicationRepeatFrequency.entries.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelected(option) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        style = SeniorOnTextStyles.BodySSemiBold,
                        color = if (isSelected) {
                            SeniorOnColors.Primary600
                        } else {
                            SeniorOnColors.Gray600
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationRepeatStepper(
    label: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    circleSize: Dp = 24.dp,
    iconSize: Dp = 10.dp,
    iconStroke: Dp = 1.5.dp,
    iconGap: Dp = 10.dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MedicationRepeatStepperCircleButton(
            size = circleSize,
            onClick = onDecrease
        ) {
            Box(
                modifier = Modifier
                    .width(iconSize)
                    .height(iconStroke)
                    .background(SeniorOnColors.Gray700)
            )
        }
        Spacer(modifier = Modifier.width(iconGap))
        Text(
            text = label,
            style = SeniorOnTextStyles.BodyMMedium,
            color = SeniorOnColors.Gray800
        )
        Spacer(modifier = Modifier.width(iconGap))
        MedicationRepeatStepperCircleButton(
            size = circleSize,
            onClick = onIncrease
        ) {
            Box(
                modifier = Modifier.size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(iconStroke)
                        .background(SeniorOnColors.Gray700)
                )
                Box(
                    modifier = Modifier
                        .width(iconStroke)
                        .fillMaxHeight()
                        .background(SeniorOnColors.Gray700)
                )
            }
        }
    }
}

@Composable
private fun MedicationRepeatStepperCircleButton(
    size: Dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .border(1.dp, SeniorOnColors.Gray200, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun MedicationRepeatWeekdaySelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MedicationWeekdayLabels.forEachIndexed { index, label ->
            val selected = index in selectedDays
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) SeniorOnColors.Primary100 else SeniorOnColors.Background3
                    )
                    .then(
                        if (selected) {
                            Modifier.border(1.dp, SeniorOnColors.Primary400, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onDayToggle(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray500
                )
            }
        }
    }
}

@Composable
private fun MedicationRepeatDurationOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = if (selected) R.drawable.ic_radio_button_1 else R.drawable.ic_radio_button_2
                ),
                contentDescription = null,
                tint = SeniorOnColors.Primary600,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray800,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = SeniorOnColors.Gray100
            )
        }
    }
}

@Composable
private fun MedicationRepeatSheetButton(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = SeniorOnTextStyles.ButtonM,
            color = contentColor
        )
    }
}

@Composable
private fun MedicationRepeatSheetPreview(
    initial: MedicationRepeatSelection,
) {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            MedicationRepeatSheetContent(
                initial = initial,
                onCancel = {},
                onConfirm = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SeniorOnColors.SupportWhite100)
            )
        }
    }
}

@Preview(
    name = "반복 - 매일 - 계속 복용",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatDailyContinuousPreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Daily,
            duration = MedicationRepeatDuration.Continuous,
        )
    )
}

@Preview(
    name = "반복 - 매일 - 복용 기간",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatDailyPeriodPreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Daily,
            duration = MedicationRepeatDuration.Period,
            periodValue = 3,
        )
    )
}

@Preview(
    name = "반복 - 매일 - 날짜 지정",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatDailyDatePreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Daily,
            duration = MedicationRepeatDuration.Date,
            endDate = LocalDate.of(2026, 6, 17),
        )
    )
}

@Preview(
    name = "반복 - 매주 - 계속 복용",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatWeeklyContinuousPreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Weekly,
            cycleValue = 1,
            weekdays = setOf(1, 2, 3, 4),
            duration = MedicationRepeatDuration.Continuous,
        )
    )
}

@Preview(
    name = "반복 - 매주 - 복용 기간",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatWeeklyPeriodPreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Weekly,
            cycleValue = 2,
            weekdays = setOf(1, 3, 5),
            duration = MedicationRepeatDuration.Period,
            periodValue = 4,
        )
    )
}

@Preview(
    name = "반복 - 매주 - 날짜 지정",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationRepeatWeeklyDatePreview() {
    MedicationRepeatSheetPreview(
        initial = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Weekly,
            cycleValue = 1,
            weekdays = setOf(1, 2, 3, 4),
            duration = MedicationRepeatDuration.Date,
            endDate = LocalDate.of(2026, 6, 17),
        )
    )
}

@Preview(
    name = "복약 종료 날짜 선택",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun MedicationEndDateCalendarDialogPreview() {
    SENIOR_ONTheme {
        MedicationEndDateCalendarDialog(
            initialDate = LocalDate.of(2026, 6, 17),
            onCancel = {},
            onConfirm = {},
        )
    }
}
