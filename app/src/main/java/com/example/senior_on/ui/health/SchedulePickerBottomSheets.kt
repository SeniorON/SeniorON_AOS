package com.example.senior_on.ui.health

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private val PickerItemHeight = 70.dp
private val PickerViewportHeight = 200.dp
private val PickerContentPadding = 65.dp

@Composable
internal fun ScheduleDatePickerBottomSheet(
    initialDate: LocalDate,
    markedDays: Set<Int> = emptySet(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    NonDraggableBottomSheet {
        ScheduleDatePickerSheetContent(
            initialDate = initialDate,
            markedDays = markedDays,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun ScheduleDatePickerSheetContent(
    initialDate: LocalDate,
    markedDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    var year by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.year) }
    var month by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.monthValue) }
    var day by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.dayOfMonth) }
    val displayedMonth = remember(year, month) { YearMonth.of(year, month) }
    val adjustedDay = day.coerceAtMost(displayedMonth.lengthOfMonth())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        ScheduleCalendar(
            displayedMonth = displayedMonth,
            selectedDay = adjustedDay,
            markedDays = markedDays,
            onDayClick = { day = it },
            onPreviousMonthClick = {
                val moved = displayedMonth.minusMonths(1)
                year = moved.year
                month = moved.monthValue
            },
            onNextMonthClick = {
                val moved = displayedMonth.plusMonths(1)
                year = moved.year
                month = moved.monthValue
            },
            headerEndContent = {
                Text(
                    text = "닫기",
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600,
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            },
            mode = ScheduleCalendarMode.BottomSheet
        )
        Spacer(modifier = Modifier.height(19.dp))
        HospitalFilledActionButton(
            label = "저장하기",
            onClick = { onConfirm(displayedMonth.atDay(adjustedDay)) }
        )
    }
}

@Composable
internal fun ScheduleTimePickerBottomSheet(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    NonDraggableBottomSheet {
        ScheduleTimePickerSheetContent(
            initialTime = initialTime,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun ScheduleTimePickerSheetContent(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val initialHour12 = (initialTime.hour % 12).let { if (it == 0) 12 else it }
    var periodIndex by rememberSaveable(initialTime) {
        mutableIntStateOf(if (initialTime.hour < 12) 0 else 1)
    }
    var hour by rememberSaveable(initialTime) { mutableIntStateOf(initialHour12) }
    var minute by rememberSaveable(initialTime) { mutableIntStateOf(initialTime.minute) }
    val periods = remember { listOf("오전", "오후") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "시간 선택",
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "닫기",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onDismiss),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WheelPicker(
                values = periods,
                selectedValue = periods[periodIndex],
                onValueSelected = { periodIndex = periods.indexOf(it) },
                modifier = Modifier.weight(1f)
            )
            WheelPicker(
                values = (1..12).toList(),
                selectedValue = hour,
                onValueSelected = { hour = it },
                modifier = Modifier.weight(1f)
            )
            WheelPicker(
                values = (0..59).toList(),
                selectedValue = minute,
                label = { it.toString().padStart(2, '0') },
                onValueSelected = { minute = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = SeniorOnColors.Primary600,
                    shape = RoundedCornerShape(SeniorOnRadius.Small)
                )
                .clickable {
                    val hour24 = when {
                        periodIndex == 0 && hour == 12 -> 0
                        periodIndex == 1 && hour != 12 -> hour + 12
                        else -> hour
                    }
                    onConfirm(LocalTime.of(hour24, minute))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "저장하기",
                style = SeniorOnTextStyles.ButtonM,
                color = SeniorOnColors.SupportWhite100
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> WheelPicker(
    values: List<T>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: (T) -> String = { it.toString() }
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, values) {
        snapshotFlow {
            val info = listState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { values.getOrNull(it)?.let(onValueSelected) }
    }

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(selectedIndex)
        }
    }

    Box(modifier = modifier.height(PickerViewportHeight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = PickerContentPadding),
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(values) { index, value ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PickerItemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(value),
                        style = SeniorOnTextStyles.HeadingXS,
                        color = if (value == selectedValue) {
                            SeniorOnColors.Gray800
                        } else {
                            SeniorOnColors.Gray200
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-30).dp)
                .fillMaxWidth()
                .height(1.5.dp)
                .background(SeniorOnColors.Primary600)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = 30.dp)
                .fillMaxWidth()
                .height(1.5.dp)
                .background(SeniorOnColors.Primary600)
        )
    }
}

@Preview(
    name = "진료일 선택 바텀시트",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ScheduleDatePickerBottomSheetPreview() {
    SENIOR_ONTheme {
        ScheduleDatePickerInlinePreview()
    }
}

@Preview(
    name = "진료 시간 선택 바텀시트",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ScheduleTimePickerBottomSheetPreview() {
    SENIOR_ONTheme {
        ScheduleTimePickerInlinePreview()
    }
}

@Composable
private fun ScheduleDatePickerInlinePreview() {
    BottomSheetPreviewSurface {
        ScheduleDatePickerSheetContent(
            initialDate = LocalDate.of(2026, 6, 17),
            markedDays = setOf(19, 22, 26),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Composable
private fun ScheduleTimePickerInlinePreview() {
    BottomSheetPreviewSurface {
        ScheduleTimePickerSheetContent(
            initialTime = LocalTime.of(14, 0),
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Composable
private fun BottomSheetPreviewSurface(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SeniorOnColors.SupportWhite100)
        ) {
            content()
        }
    }
}
