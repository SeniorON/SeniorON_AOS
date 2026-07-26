package com.example.senior_on.ui.common.seniorinfo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val ParentInfoMinBirthYear = 1900
private val ParentInfoDefaultBirthDate = LocalDate.of(1933, 5, 12)
private val ParentInfoWheelItemHeight = 70.dp
private val ParentInfoWheelHeight = 200.dp
private val ParentInfoWheelContentPadding = 65.dp
private val ParentInfoWheelDividerOffset = 30.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParentInfoBirthDateBottomSheet(
    initialBirthDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SeniorOnColors.SupportWhite100,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.5f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        ),
    ) {
        ParentInfoBirthDateSheetContent(
            initialBirthDate = initialBirthDate,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun ParentInfoBirthDateSheetContent(
    initialBirthDate: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val initialDate = remember(initialBirthDate, today) {
        parseBirthDate(initialBirthDate)
            ?: ParentInfoDefaultBirthDate.coerceAtMost(today)
    }
    var selectedYear by rememberSaveable { mutableStateOf(initialDate.year) }
    var selectedMonth by rememberSaveable { mutableStateOf(initialDate.monthValue) }
    var selectedDay by rememberSaveable { mutableStateOf(initialDate.dayOfMonth) }

    val years = remember(today.year) {
        (ParentInfoMinBirthYear..today.year).toList()
    }
    val months = remember(selectedYear, today) {
        val lastMonth = if (selectedYear == today.year) today.monthValue else 12
        (1..lastMonth).toList()
    }
    val adjustedMonth = selectedMonth.coerceIn(months.first(), months.last())

    LaunchedEffect(adjustedMonth) {
        selectedMonth = adjustedMonth
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
        selectedDay = adjustedDay
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "생년월일",
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800,
            )
            Text(
                text = "닫기",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 4.dp),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ParentInfoDateWheelPicker(
                values = years,
                selectedValue = selectedYear,
                valueLabel = Int::toString,
                onValueSelected = { selectedYear = it },
                modifier = Modifier.weight(1f),
            )
            ParentInfoDateWheelPicker(
                values = months,
                selectedValue = adjustedMonth,
                valueLabel = { "${it}월" },
                onValueSelected = { selectedMonth = it },
                modifier = Modifier.weight(1f),
            )
            ParentInfoDateWheelPicker(
                values = days,
                selectedValue = adjustedDay,
                valueLabel = Int::toString,
                onValueSelected = { selectedDay = it },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        SeniorInfoActionButton(
            text = "확인",
            onClick = {
                onConfirm(
                    LocalDate.of(
                        selectedYear,
                        adjustedMonth,
                        adjustedDay,
                    ),
                )
            },
            style = SeniorInfoButtonStyle.Filled,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParentInfoDateWheelPicker(
    values: List<Int>,
    selectedValue: Int,
    valueLabel: (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex,
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

    Box(
        modifier = modifier.height(ParentInfoWheelHeight),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = ParentInfoWheelContentPadding),
            flingBehavior = flingBehavior,
        ) {
            itemsIndexed(
                items = values,
                key = { _, value -> value },
            ) { index, value ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ParentInfoWheelItemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = valueLabel(value),
                        style = SeniorOnTextStyles.HeadingXS,
                        color = if (value == selectedValue) {
                            SeniorOnColors.Gray800
                        } else {
                            SeniorOnColors.Gray200
                        },
                    )
                }
            }
        }

        ParentInfoDateWheelDivider(offset = -ParentInfoWheelDividerOffset)
        ParentInfoDateWheelDivider(offset = ParentInfoWheelDividerOffset)
    }
}

@Composable
private fun BoxScope.ParentInfoDateWheelDivider(offset: Dp) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(y = offset)
            .fillMaxWidth()
            .height(1.5.dp)
            .background(SeniorOnColors.Primary600),
    )
}
