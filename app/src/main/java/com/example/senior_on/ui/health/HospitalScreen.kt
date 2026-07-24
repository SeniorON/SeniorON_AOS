package com.example.senior_on.ui.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun HospitalScreen(
    modifier: Modifier = Modifier,
    appointments: List<HospitalAppointmentUiState> = previewHospitalAppointments(),
    initialMonth: YearMonth = YearMonth.of(2026, 6),
    initialSelectedDay: Int = 17,
    onAddAppointmentClick: (LocalDate) -> Unit = {},
    onAppointmentClick: (HospitalAppointmentUiState) -> Unit = {},
    onEditAppointmentClick: (HospitalAppointmentUiState) -> Unit = {},
    onDeleteAppointmentClick: (HospitalAppointmentUiState) -> Unit = {}
) {
    var displayedYear by rememberSaveable { mutableIntStateOf(initialMonth.year) }
    var displayedMonthValue by rememberSaveable { mutableIntStateOf(initialMonth.monthValue) }
    var selectedDay by rememberSaveable { mutableIntStateOf(initialSelectedDay) }
    val displayedMonth = remember(displayedYear, displayedMonthValue) {
        YearMonth.of(displayedYear, displayedMonthValue)
    }
    val appointmentDays = remember(appointments, displayedMonth) {
        appointments
            .filter { YearMonth.from(it.date) == displayedMonth }
            .mapTo(mutableSetOf()) { it.date.dayOfMonth }
    }
    val selectedDate = displayedMonth.atDay(selectedDay)
    val selectedAppointments = appointments.filter { it.date == selectedDate }

    fun moveMonth(monthDelta: Long) {
        val movedMonth = displayedMonth.plusMonths(monthDelta)
        displayedYear = movedMonth.year
        displayedMonthValue = movedMonth.monthValue
        selectedDay = selectedDay.coerceAtMost(movedMonth.lengthOfMonth())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .verticalScroll(rememberScrollState())
    ) {
        UpcomingAppointmentsSection(
            appointments = appointments,
            onAppointmentClick = onAppointmentClick
        )
        HospitalScheduleSection(
            displayedMonth = displayedMonth,
            selectedDay = selectedDay,
            appointmentDays = appointmentDays,
            selectedAppointments = selectedAppointments,
            onDayClick = { selectedDay = it },
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
        date = LocalDate.of(2026, 6, 22),
        hospitalName = "서울대학교병원",
        specialty = "내과",
        time = LocalTime.of(10, 30),
        daysLeft = 8,
        highlighted = true
    ),
    HospitalAppointmentUiState(
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
        HospitalScreen(initialSelectedDay = 22)
    }
}
