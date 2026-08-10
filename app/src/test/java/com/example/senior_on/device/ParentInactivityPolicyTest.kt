package com.example.senior_on.device

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentInactivityPolicyTest {
    @Test
    fun `creates an event when the configured threshold has elapsed`() {
        val lastActiveAt = 1_000L
        val state = state(lastActiveAtMillis = lastActiveAt, thresholdHours = 4)

        assertTrue(
            ParentInactivityPolicy.shouldCreateEvent(
                state = state,
                nowMillis = lastActiveAt + TimeUnit.HOURS.toMillis(4),
            )
        )
    }

    @Test
    fun `does not create an event before the threshold`() {
        val lastActiveAt = 1_000L
        val state = state(lastActiveAtMillis = lastActiveAt, thresholdHours = 4)

        assertFalse(
            ParentInactivityPolicy.shouldCreateEvent(
                state = state,
                nowMillis = lastActiveAt + TimeUnit.HOURS.toMillis(4) - 1,
            )
        )
    }

    @Test
    fun `does not create another event for the same activity period`() {
        val lastActiveAt = 1_000L
        val state = state(
            lastActiveAtMillis = lastActiveAt,
            alertedForActivityAtMillis = lastActiveAt,
        )

        assertFalse(
            ParentInactivityPolicy.shouldCreateEvent(
                state = state,
                nowMillis = lastActiveAt + TimeUnit.HOURS.toMillis(24),
            )
        )
    }

    @Test
    fun `does not create an event when monitoring is disabled`() {
        val lastActiveAt = 1_000L
        val state = state(lastActiveAtMillis = lastActiveAt, enabled = false)

        assertFalse(
            ParentInactivityPolicy.shouldCreateEvent(
                state = state,
                nowMillis = lastActiveAt + TimeUnit.HOURS.toMillis(24),
            )
        )
    }

    private fun state(
        lastActiveAtMillis: Long?,
        thresholdHours: Int = 4,
        enabled: Boolean = true,
        alertedForActivityAtMillis: Long? = null,
    ) = ParentInactivityState(
        lastActiveAtMillis = lastActiveAtMillis,
        thresholdHours = thresholdHours,
        enabled = enabled,
        alertedForActivityAtMillis = alertedForActivityAtMillis,
    )
}
