package com.example.senior_on.ui.child.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.core.time.koreaToday
import com.example.senior_on.core.time.koreaYearMonth
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun HospitalScreen(
    modifier: Modifier = Modifier,
    appointments: List<HospitalAppointmentUiState> = previewHospitalAppointments(),
    upcomingAppointments: List<HospitalAppointmentUiState> = appointments,
    displayedMonth: YearMonth = koreaYearMonth(),
    selectedDate: LocalDate = koreaToday(),
    selectedAppointments: List<HospitalAppointmentUiState> = appointments.filter { it.date == selectedDate },
    onDisplayedMonthChange: (YearMonth) -> Unit = {},
    onSelectedDateChange: (LocalDate) -> Unit = {},
    onAddAppointmentClick: (LocalDate) -> Unit = {},
    onAppointmentClick: (HospitalAppointmentUiState) -> Unit = {},
    onEditAppointmentClick: (HospitalAppointmentUiState) -> Unit = {},
    onDeleteAppointmentClick: (HospitalAppointmentUiState) -> Unit = {},
) {
    val appointmentDays = remember(appointments, displayedMonth) {
        appointments
            .filter { YearMonth.from(it.date) == displayedMonth }
            .mapTo(mutableSetOf()) { it.date.dayOfMonth }
    }
    fun moveMonth(monthDelta: Long) {
        val movedMonth = displayedMonth.plusMonths(monthDelta)
        onDisplayedMonthChange(movedMonth)
        val day = selectedDate.dayOfMonth.coerceAtMost(movedMonth.lengthOfMonth())
        onSelectedDateChange(movedMonth.atDay(day))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .verticalScroll(rememberScrollState())
    ) {
        UpcomingAppointmentsSection(
            appointments = upcomingAppointments,
            onAppointmentClick = onAppointmentClick
        )
        HospitalScheduleSection(
            displayedMonth = displayedMonth,
            selectedDay = selectedDate.dayOfMonth,
            appointmentDays = appointmentDays,
            selectedAppointments = selectedAppointments,
            onDayClick = { day ->
                onSelectedDateChange(displayedMonth.atDay(day))
            },
            onAddAppointmentClick = { onAddAppointmentClick(selectedDate) },
            onEditAppointmentClick = onEditAppointmentClick,
            onDeleteAppointmentClick = onDeleteAppointmentClick,
            onPreviousMonthClick = { moveMonth(-1) },
            onNextMonthClick = { moveMonth(1) }
        )
    }
}

internal fun previewHospitalAppointments() = listOf(
    HospitalAppointmentUiState(
        id = 1L,
        date = LocalDate.of(2026, 6, 22),
        hospitalName = "서울대학교병원",
        specialty = "내과",
        time = LocalTime.of(10, 30),
        daysLeft = 8,
        highlighted = true
    ),
    HospitalAppointmentUiState(
        id = 2L,
        date = LocalDate.of(2026, 6, 26),
        hospitalName = "연세세브란스병원",
        specialty = "정형외과",
        time = LocalTime.of(14, 0),
        daysLeft = 12
    )
)

@Preview(name = "Hospital - Initial", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HospitalScreenPreview() {
    SENIOR_ONTheme {
        HospitalScreen()
    }
}

@Preview(name = "Hospital - Selected Appointment", showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun HospitalSelectedAppointmentPreview() {
    SENIOR_ONTheme {
        HospitalScreen(selectedDate = LocalDate.of(2026, 6, 22))
    }
}
