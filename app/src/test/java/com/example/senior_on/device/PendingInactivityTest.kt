package com.example.senior_on.device

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class PendingInactivityTest {
    private val event = PendingInactivity("parent-a", 100, 200)
    private val state = ParentInactivityState(100, 3, true, null)

    @Test fun expires48HoursAfterQueueingNotLastActivity() {
        assertFalse(event.isExpired(200 + TimeUnit.HOURS.toMillis(48) - 1))
        assertTrue(event.isExpired(200 + TimeUnit.HOURS.toMillis(48)))
    }

    @Test fun newActivityInvalidatesPendingEvent() {
        assertTrue(event.isCurrent("parent-a", state))
        assertFalse(event.isCurrent("parent-a", state.copy(lastActiveAtMillis = 101)))
    }

    @Test fun anotherAccountCannotReuseEvent() {
        assertFalse(event.isCurrent("parent-b", state))
    }

    @Test fun missingBaselineCannotReuseEvent() {
        assertFalse(event.isCurrent("parent-a", state.copy(lastActiveAtMillis = null)))
    }
}
