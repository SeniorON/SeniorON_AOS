package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.core.time.koreaToday
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    registeredMedications: List<RegisteredMedicationUiState> = previewRegisteredMedications(),
    todayMedications: List<TodayMedicationUiState> = previewTodayMedications(),
    medicationMarkedDates: Set<LocalDate> = previewMedicationMarkedDates(),
    selectedDate: LocalDate = koreaToday(),
    onSelectedDateChange: (LocalDate) -> Unit = {},
    onAddTodayMedicationClick: () -> Unit = {},
    onAddRegisteredMedicationClick: () -> Unit = {},
    onRegisteredMedicationClick: (RegisteredMedicationUiState) -> Unit = {},
    scheduleMessage: String? = null,
    onCalendarMonthChange: (YearMonth) -> Unit = {},
    calendarLoading: Boolean = false,
    calendarError: String? = null,
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var displayedMonthText by rememberSaveable(selectedDate) {
        mutableStateOf(YearMonth.from(selectedDate).toString())
    }
    val displayedMonth = YearMonth.parse(displayedMonthText)
    LaunchedEffect(showCalendar, displayedMonth) {
        if (showCalendar) onCalendarMonthChange(displayedMonth)
    }
    val activeRegisteredMedications = remember(registeredMedications, selectedDate) {
        registeredMedications.filter { medication ->
            medication.isActiveOn(selectedDate)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.SupportWhite100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TodayMedicationSection(
                selectedDate = selectedDate,
                todayMedications = todayMedications,
                scheduleMessage = scheduleMessage,
                showCalendar = showCalendar,
                onYearClick = { showCalendar = !showCalendar },
                onPreviousDayClick = {
                    onSelectedDateChange(selectedDate.minusDays(1))
                },
                onNextDayClick = {
                    onSelectedDateChange(selectedDate.plusDays(1))
                },
                onAddTodayMedicationClick = onAddTodayMedicationClick
            )

            RegisteredMedicationsSection(
                medications = activeRegisteredMedications,
                onAddMedicationClick = onAddRegisteredMedicationClick,
                onMedicationClick = onRegisteredMedicationClick
            )
        }

        if (showCalendar) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showCalendar = false }
                    )
            )

            HealthCalendarCard(
                displayedMonth = displayedMonth,
                selectedDay = selectedDate.dayOfMonth.takeIf { YearMonth.from(selectedDate) == displayedMonth },
                markedDates = medicationMarkedDates,
                onDayClick = { day ->
                    onSelectedDateChange(displayedMonth.atDay(day))
                    showCalendar = false
                },
                onPreviousMonthClick = {
                    displayedMonthText = displayedMonth.minusMonths(1).toString()
                },
                onNextMonthClick = {
                    displayedMonthText = displayedMonth.plusMonths(1).toString()
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = HealthCalendarCardTopOffset)
                    .zIndex(2f)
            )
            if (calendarLoading || calendarError != null) {
                androidx.compose.material3.Text(
                    text = if (calendarLoading) "일정 표시를 불러오는 중이에요" else calendarError.orEmpty(),
                    style = com.example.senior_on.ui.theme.SeniorOnTextStyles.BodySRegular,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(top = HealthCalendarCardTopOffset + HealthCalendarCardHeight)
                        .zIndex(2f)
                        .background(SeniorOnColors.SupportWhite100)
                        .clickable(enabled = !calendarLoading) { onCalendarMonthChange(displayedMonth) },
                )
            }
        }
    }
}

@Preview(name = "Health Tab - Full", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun HealthScreenFullPreview() {
    SENIOR_ONTheme {
        HealthScreen()
    }
}

@Preview(name = "Health Tab - Empty", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun HealthScreenEmptyPreview() {
    SENIOR_ONTheme {
        HealthScreen(
            registeredMedications = emptyList(),
            todayMedications = emptyList(),
            medicationMarkedDates = emptySet()
        )
    }
}

@Preview(name = "Health Tab - Single Medication", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun HealthScreenSingleMedicationPreview() {
    SENIOR_ONTheme {
        HealthScreen(
            registeredMedications = previewRegisteredMedications().take(1),
            todayMedications = emptyList()
        )
    }
}
