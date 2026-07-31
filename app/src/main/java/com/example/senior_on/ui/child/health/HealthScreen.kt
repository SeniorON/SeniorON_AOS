package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    medicationMarkedDates: Set<LocalDate> = previewMedicationMarkedDates(),
    selectedDate: LocalDate = LocalDate.now(),
    onSelectedDateChange: (LocalDate) -> Unit = {},
    onAddTodayMedicationClick: () -> Unit = {},
    onAddRegisteredMedicationClick: () -> Unit = {},
    onRegisteredMedicationClick: (RegisteredMedicationUiState) -> Unit = {}
) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val displayedMonth = remember(selectedDate) { YearMonth.from(selectedDate) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .verticalScroll(rememberScrollState())
    ) {
        TodayMedicationSection(
            selectedDate = selectedDate,
            todayMedications = todayMedications,
            markedDates = medicationMarkedDates,
            showCalendar = showCalendar,
            onYearClick = { showCalendar = !showCalendar },
            onPreviousDayClick = {
                onSelectedDateChange(selectedDate.minusDays(1))
            },
            onNextDayClick = {
                onSelectedDateChange(selectedDate.plusDays(1))
            },
            onDayClick = { day ->
                onSelectedDateChange(displayedMonth.atDay(day))
                showCalendar = false
            },
            onPreviousMonthClick = {
                val movedMonth = displayedMonth.minusMonths(1)
                onSelectedDateChange(
                    movedMonth.atDay(selectedDate.dayOfMonth.coerceAtMost(movedMonth.lengthOfMonth()))
                )
            },
            onNextMonthClick = {
                val movedMonth = displayedMonth.plusMonths(1)
                onSelectedDateChange(
                    movedMonth.atDay(selectedDate.dayOfMonth.coerceAtMost(movedMonth.lengthOfMonth()))
                )
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
