package com.example.senior_on.domain.model.parent

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentMedicationDeadlineTest {
    private val date = LocalDate.of(2026, 9, 29)
    private val medication = ParentMedication("1", "약", LocalTime.of(8, 0), scheduledDate = date)

    @Test fun beforeDeadline() {
        assertFalse(medication.isTakingDeadlineReached(date.atTime(9, 59, 59)))
    }

    @Test fun exactDeadline() {
        assertFalse(medication.isTakingDeadlineReached(date.atTime(10, 0)))
    }

    @Test fun afterDeadline() {
        assertTrue(medication.isTakingDeadlineReached(date.atTime(10, 0, 1)))
    }

    @Test fun deadlineCrossingMidnight() {
        val lateDose = medication.copy(scheduledTime = LocalTime.of(23, 0))
        assertFalse(lateDose.isTakingDeadlineReached(date.plusDays(1).atTime(0, 59, 59)))
        assertFalse(lateDose.isTakingDeadlineReached(date.plusDays(1).atTime(1, 0)))
        assertTrue(lateDose.isTakingDeadlineReached(date.plusDays(1).atTime(1, 0, 0, 1)))
        assertTrue(lateDose.isTakingDeadlineReached(date.plusDays(1).atTime(23, 0)))
    }
}
