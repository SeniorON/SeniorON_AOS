package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val MinBirthYear = 1900
private const val DefaultBirthAge = 70
private val DateWheelItemHeight = 44.dp
private val DateWheelHeight = 132.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthDateBottomSheet(
    initialBirthDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.45f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge
        )
    ) {
        BirthDateSheetContent(
            initialBirthDate = initialBirthDate,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
internal fun BirthDateSheetContent(
    initialBirthDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val initialDate = remember(initialBirthDate, today) {
        parseBirthDate(initialBirthDate)
            ?: LocalDate.of(
                (today.year - DefaultBirthAge).coerceAtLeast(MinBirthYear),
                1,
                1
            )
    }
    var selectedYear by rememberSaveable { mutableStateOf(initialDate.year) }
    var selectedMonth by rememberSaveable { mutableStateOf(initialDate.monthValue) }
    var selectedDay by rememberSaveable { mutableStateOf(initialDate.dayOfMonth) }

    val years = remember(today.year) {
        (MinBirthYear..today.year).toList()
    }
    val months = remember(selectedYear, today) {
        val lastMonth = if (selectedYear == today.year) today.monthValue else 12
        (1..lastMonth).toList()
    }
    val adjustedMonth = selectedMonth.coerceIn(months.first(), months.last())

    LaunchedEffect(adjustedMonth) {
        if (selectedMonth != adjustedMonth) {
            selectedMonth = adjustedMonth
        }
    }

    val days = remember(selectedYear, adjustedMonth, today) {
        val lastDayOfMonth = YearMonth.of(selectedYear, adjustedMonth).lengthOfMonth()
        val lastDay = if (
            selectedYear == today.year &&
            adjustedMonth == today.monthValue
        ) {
            today.dayOfMonth
        } else {
            lastDayOfMonth
        }
        (1..lastDay).toList()
    }
    val adjustedDay = selectedDay.coerceIn(days.first(), days.last())

    LaunchedEffect(adjustedDay) {
        if (selectedDay != adjustedDay) {
            selectedDay = adjustedDay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 10.dp, bottom = 26.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            Text(
                text = "생년월일",
                modifier = Modifier.align(Alignment.Center),
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "닫기",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DateWheelHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DateWheelItemHeight)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .background(SeniorOnColors.Background3)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement =
                    androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                DateWheelPicker(
                    values = years,
                    selectedValue = selectedYear,
                    valueLabel = Int::toString,
                    onValueSelected = { selectedYear = it },
                    modifier = Modifier.weight(1f)
                )
                DateWheelPicker(
                    values = months,
                    selectedValue = adjustedMonth,
                    valueLabel = { "${it}월" },
                    onValueSelected = { selectedMonth = it },
                    modifier = Modifier.weight(1f)
                )
                DateWheelPicker(
                    values = days,
                    selectedValue = adjustedDay,
                    valueLabel = Int::toString,
                    onValueSelected = { selectedDay = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        SeniorInfoActionButton(
            text = "확인",
            onClick = {
                onConfirm(
                    LocalDate.of(
                        selectedYear,
                        adjustedMonth,
                        adjustedDay
                    )
                )
            },
            style = SeniorInfoButtonStyle.Filled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateWheelPicker(
    values: List<Int>,
    selectedValue: Int,
    valueLabel: (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex
    )
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, values) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (
                layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset
                ) / 2

            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - viewportCenter)
            }?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { index ->
                values.getOrNull(index)?.let(onValueSelected)
            }
    }

    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(selectedIndex)
        }
    }

    Box(modifier = modifier.height(DateWheelHeight)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = DateWheelItemHeight),
            flingBehavior = flingBehavior
        ) {
            itemsIndexed(
                items = values,
                key = { _, value -> value }
            ) { index, value ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DateWheelItemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = valueLabel(value),
                        style = if (value == selectedValue) {
                            SeniorOnTextStyles.BodyLBold
                        } else {
                            SeniorOnTextStyles.BodyLMedium
                        },
                        color = if (value == selectedValue) {
                            SeniorOnColors.Gray800
                        } else {
                            SeniorOnColors.Gray400
                        }
                    )
                }
            }
        }
    }
}
