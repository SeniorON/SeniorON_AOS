package com.example.senior_on.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupNameBirthScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var isBirthSheetVisible by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val canGoNext = name.isNotBlank() && birthDate.isNotBlank()

    SignupStepScaffold(
        progress = 1 / 4f,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "이름과 생년월일을\n입력해 주세요",
            style = SeniorOnTextStyles.OnboardingHeading,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        SignupUnderlinedTextField(
            label = "이름",
            value = name,
            onValueChange = { name = it.take(20) },
            placeholder = "이름 입력(20자 이내)"
        )

        Spacer(modifier = Modifier.height(30.dp))

        SignupBirthDateField(
            value = birthDate,
            onClick = { isBirthSheetVisible = true }
        )

        Spacer(modifier = Modifier.weight(1f))

        SignupNextButton(
            enabled = canGoNext,
            onClick = onNextClick
        )

        Spacer(modifier = Modifier.height(22.5.dp))
    }

    if (isBirthSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isBirthSheetVisible = false },
            sheetState = sheetState,
            containerColor = SeniorOnColors.SupportWhite100,
            scrimColor = Color(0x80000000),
            dragHandle = null,
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            )
        ) {
            SignupBirthDateBottomSheet(
                selectedYear = 1933,
                selectedMonth = 5,
                selectedDay = 12,
                onCloseClick = { isBirthSheetVisible = false },
                onConfirmClick = { year, month, day ->
                    birthDate = "%04d.%02d.%02d".format(year, month, day)
                    isBirthSheetVisible = false
                }
            )
        }
    }
}

@Composable
private fun SignupBirthDateField(
    value: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "생년월일",
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(43.dp)
                .clickable(onClick = onClick)
                .padding(start = 6.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifBlank { "생년월일 선택" },
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMMedium,
                color = if (value.isBlank()) SeniorOnColors.Gray300 else SeniorOnColors.Gray800
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_sm_chevron_down_2),
                contentDescription = "생년월일 선택",
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SeniorOnColors.Gray200)
        )
    }
}

@Composable
private fun SignupBirthDateBottomSheet(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onCloseClick: () -> Unit,
    onConfirmClick: (year: Int, month: Int, day: Int) -> Unit
) {
    val years = remember { (1900..2026).toList() }
    val months = remember { (1..12).toList() }
    val days = remember { (1..31).toList() }
    val yearState = rememberLazyListState(
        initialFirstVisibleItemIndex = years.indexOf(selectedYear).coerceAtLeast(0)
    )
    val monthState = rememberLazyListState(
        initialFirstVisibleItemIndex = months.indexOf(selectedMonth).coerceAtLeast(0)
    )
    val dayState = rememberLazyListState(
        initialFirstVisibleItemIndex = days.indexOf(selectedDay).coerceAtLeast(0)
    )
    val pickedYear by remember {
        derivedStateOf {
            years[yearState.firstVisibleItemIndex.coerceIn(years.indices)]
        }
    }
    val pickedMonth by remember {
        derivedStateOf {
            months[monthState.firstVisibleItemIndex.coerceIn(months.indices)]
        }
    }
    val pickedDay by remember {
        derivedStateOf {
            days[dayState.firstVisibleItemIndex.coerceIn(days.indices)]
        }
    }

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
                text = "생년월일",
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800
            )

            Text(
                text = "닫기",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onCloseClick),
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Primary600
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SignupBirthPickerColumn(
                values = years,
                selectedValue = pickedYear,
                listState = yearState,
                textFormatter = { "$it" },
                modifier = Modifier.weight(1f)
            )

            SignupBirthPickerColumn(
                values = months,
                selectedValue = pickedMonth,
                listState = monthState,
                textFormatter = { "${it}월" },
                modifier = Modifier.weight(1f)
            )

            SignupBirthPickerColumn(
                values = days,
                selectedValue = pickedDay,
                listState = dayState,
                textFormatter = { "$it" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(SeniorOnColors.Primary600, RoundedCornerShape(SeniorOnRadius.Small))
                .clickable { onConfirmClick(pickedYear, pickedMonth, pickedDay) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "확인",
                style = SeniorOnTextStyles.ButtonM,
                color = SeniorOnColors.SupportWhite100
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SignupBirthPickerColumn(
    values: List<Int>,
    selectedValue: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    textFormatter: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val pickerHeight = 200.dp
    val pickerItemHeight = 70.dp
    val pickerCenterPadding = (pickerHeight - pickerItemHeight) / 2

    Box(modifier = modifier.height(pickerHeight)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = pickerCenterPadding),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(values.size) { index ->
                val value = values[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pickerItemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = textFormatter(value),
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
                .offset(y = -30.dp)
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
    name = "Signup Name Birth",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupNameBirthScreenPreview() {
    SENIOR_ONTheme {
        SignupNameBirthScreen(
            onBackClick = {},
            onNextClick = {}
        )
    }
}

@Preview(
    name = "Signup Birth Bottom Sheet",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SignupBirthDateBottomSheetPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        SeniorOnColors.SupportWhite100,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                SignupBirthDateBottomSheet(
                    selectedYear = 1980,
                    selectedMonth = 5,
                    selectedDay = 12,
                    onCloseClick = {},
                    onConfirmClick = { _, _, _ -> }
                )
            }
        }
    }
}
