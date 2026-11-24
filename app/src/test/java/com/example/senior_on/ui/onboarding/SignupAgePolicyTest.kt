package com.example.senior_on.ui.onboarding

import com.example.senior_on.domain.model.auth.SignupAgePolicy
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class SignupAgePolicyTest {
    private val today = LocalDate.of(2026, 11, 24)

    @Test fun birthdayBoundaryAppliesToDisplayAndApiFormats() {
        assertNull(SignupAgePolicy.error("2012.11.24", today))
        assertNull(SignupAgePolicy.error("2012-11-23", today))
        assertEquals(SignupAgePolicy.ageMessage, SignupAgePolicy.error("2012-11-25", today))
        assertNull(SignupAgePolicy.error("2012-11-25", today.plusDays(1)))
    }

    @Test fun leapBirthdayMatchesSocialSignupServer() {
        assertNotNull(SignupAgePolicy.error("2012-02-29", LocalDate.of(2026, 2, 27)))
        assertNull(SignupAgePolicy.error("2012-02-29", LocalDate.of(2026, 2, 28)))
    }

    @Test fun invalidFutureAndEmptyDatesCannotPass() {
        listOf("2010.02.31", "2011-02-29", "2026.12.10", "").forEach {
            assertNotNull(SignupAgePolicy.error(it, today))
        }
    }
}
