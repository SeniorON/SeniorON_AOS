package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.MedicationMonthlyScheduleResponse
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationMonthlyScheduleMappingTest {
    @Test
    fun `monthly schedule maps latest scheduledDates field`() {
        val response = MedicationMonthlyScheduleResponse(
            year = 2026,
            month = 8,
            scheduledDates = listOf("2026-08-01", "2026-08-08", "2026-08-08"),
        )

        val result = response.toDomain(requestedYear = 2026, requestedMonth = 8)

        assertEquals(2026, result.year)
        assertEquals(8, result.month)
        assertEquals(
            setOf(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 8)),
            result.scheduledDates,
        )
    }

    @Test
    fun `monthly schedule ignores malformed and out of requested month dates`() {
        val response = MedicationMonthlyScheduleResponse(
            year = 2026,
            month = 8,
            scheduledDates = listOf("2026-07-31", "invalid", " 2026-08-03 ", "2026-09-01"),
        )

        val result = response.toDomain(requestedYear = 2026, requestedMonth = 8)

        assertEquals(setOf(LocalDate.of(2026, 8, 3)), result.scheduledDates)
    }
}
