package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    registeredMedications: List<RegisteredMedicationUiState> = previewRegisteredMedications(),
    todayMedications: List<TodayMedicationUiState> = previewTodayMedications(),
    medicationMarkedDays: Set<Int> = previewMedicationMarkedDays(),
    initialDate: LocalDate = LocalDate.of(2026, 6, 12),
    onAddTodayMedicationClick: () -> Unit = {},
    onAddRegisteredMedicationClick: () -> Unit = {},
    onRegisteredMedicationClick: (RegisteredMedicationUiState) -> Unit = {}
) {
    var selectedYear by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.year) }
    var selectedMonth by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.monthValue) }
    var selectedDay by rememberSaveable(initialDate) { mutableIntStateOf(initialDate.dayOfMonth) }
    var showCalendar by rememberSaveable { mutableStateOf(false) }

    val selectedDate = remember(selectedYear, selectedMonth, selectedDay) {
        YearMonth.of(selectedYear, selectedMonth)
            .atDay(selectedDay.coerceAtMost(YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()))
    }
    val displayedMonth = remember(selectedYear, selectedMonth) {
        YearMonth.of(selectedYear, selectedMonth)
    }

    fun updateSelectedDate(date: LocalDate) {
        selectedYear = date.year
        selectedMonth = date.monthValue
        selectedDay = date.dayOfMonth
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .verticalScroll(rememberScrollState())
    ) {
        TodayMedicationSection(
            selectedDate = selectedDate,
            todayMedications = todayMedications,
            markedDays = medicationMarkedDays,
            showCalendar = showCalendar,
            onYearClick = { showCalendar = !showCalendar },
            onPreviousDayClick = {
                updateSelectedDate(selectedDate.minusDays(1))
            },
            onNextDayClick = {
                updateSelectedDate(selectedDate.plusDays(1))
            },
            onDayClick = { day ->
                updateSelectedDate(displayedMonth.atDay(day))
                showCalendar = false
            },
            onPreviousMonthClick = {
                val movedMonth = displayedMonth.minusMonths(1)
                selectedYear = movedMonth.year
                selectedMonth = movedMonth.monthValue
                selectedDay = selectedDay.coerceAtMost(movedMonth.lengthOfMonth())
            },
            onNextMonthClick = {
                val movedMonth = displayedMonth.plusMonths(1)
                selectedYear = movedMonth.year
                selectedMonth = movedMonth.monthValue
                selectedDay = selectedDay.coerceAtMost(movedMonth.lengthOfMonth())
            },
            onAddTodayMedicationClick = onAddTodayMedicationClick
        )

        RegisteredMedicationsSection(
            medications = registeredMedications,
            onAddMedicationClick = onAddRegisteredMedicationClick,
            onMedicationClick = onRegisteredMedicationClick
        )
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
            medicationMarkedDays = emptySet()
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
