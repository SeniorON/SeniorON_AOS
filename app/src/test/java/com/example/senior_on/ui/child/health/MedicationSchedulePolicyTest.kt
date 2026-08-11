package com.example.senior_on.ui.child.health

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationSchedulePolicyTest {
    @Test
    fun `매일 복용은 서버 요일 목록이 비어 있어도 시작일부터 표시한다`() {
        val medication = medication(
            weekdays = emptySet(),
            startDate = LocalDate.of(2026, 8, 10),
            repeat = MedicationRepeatSelection(
                frequency = MedicationRepeatFrequency.Daily,
                cycleValue = 1,
            ),
        )

        assertFalse(medication.isScheduledOn(LocalDate.of(2026, 8, 9)))
        assertTrue(medication.isScheduledOn(LocalDate.of(2026, 8, 10)))
        assertTrue(medication.isScheduledOn(LocalDate.of(2026, 8, 11)))
    }

    @Test
    fun `격주 반복은 시작일이 속한 주를 기준으로 계산한다`() {
        val medication = medication(
            weekdays = setOf(1),
            startDate = LocalDate.of(2026, 8, 5),
            repeat = MedicationRepeatSelection(
                frequency = MedicationRepeatFrequency.Weekly,
                cycleValue = 2,
                weekdays = setOf(1),
            ),
        )

        assertFalse(medication.isScheduledOn(LocalDate.of(2026, 8, 10)))
        assertTrue(medication.isScheduledOn(LocalDate.of(2026, 8, 17)))
    }

    @Test
    fun `일주 복용 기간의 마지막 날은 시작일부터 칠 일째 전날이다`() {
        val medication = medication(
            weekdays = (0..6).toSet(),
            startDate = LocalDate.of(2026, 8, 1),
            repeat = MedicationRepeatSelection(
                frequency = MedicationRepeatFrequency.Daily,
                duration = MedicationRepeatDuration.Period,
                periodValue = 1,
            ),
        )

        assertTrue(medication.isScheduledOn(LocalDate.of(2026, 8, 7)))
        assertFalse(medication.isScheduledOn(LocalDate.of(2026, 8, 8)))
    }

    @Test
    fun `일일 API 응답이 있으면 등록 약의 로컬 계산보다 우선한다`() {
        val date = LocalDate.of(2026, 8, 10)
        val remote = TodayMedicationUiState(
            date = date,
            category = "혈압약",
            name = "아암로디핀",
            time = LocalTime.of(14, 0),
            status = MedicationDoseStatus.Taken,
            medicationLogId = 42L,
        )
        val futureMedication = medication(
            weekdays = (0..6).toSet(),
            startDate = date.plusDays(1),
        )

        val actual = buildTodayMedicationsFromRegistered(
            date = date,
            registered = listOf(futureMedication),
            remoteSchedules = listOf(remote),
        )

        assertEquals(listOf(remote), actual)
        assertEquals(
            emptyList<TodayMedicationUiState>(),
            buildTodayMedicationsFromRegistered(
                date = date,
                registered = listOf(futureMedication.copy(startDate = date)),
                remoteSchedules = emptyList(),
            ),
        )
    }

    private fun medication(
        weekdays: Set<Int>,
        startDate: LocalDate,
        repeat: MedicationRepeatSelection = MedicationRepeatSelection(
            frequency = MedicationRepeatFrequency.Daily,
            weekdays = weekdays,
        ),
    ) = RegisteredMedicationUiState(
        id = "group-1",
        category = "혈압약",
        name = "아암로디핀",
        times = listOf(LocalTime.of(8, 0)),
        weekdays = weekdays,
        startDate = startDate,
        repeat = repeat,
    )
}
