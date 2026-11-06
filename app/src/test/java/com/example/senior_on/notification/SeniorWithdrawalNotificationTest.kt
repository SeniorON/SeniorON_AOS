package com.example.senior_on.notification

import org.junit.Assert.*
import org.junit.Test

class SeniorWithdrawalNotificationTest {
    @Test fun notificationTapParsesNoticeAndSeniorWithoutEventId() {
        try {
            NotificationNavigationEventStore.publish(mapOf(
                "type" to "SENIOR_WITHDRAWAL", "withdrawalNoticeId" to "42", "seniorId" to "3",
            ))
            val event = NotificationNavigationEventStore.pendingEvent.value!!
            assertEquals("SENIOR_WITHDRAWAL", event.type)
            assertEquals(42L, event.withdrawalNoticeId)
            assertEquals(3L, event.seniorId)
            assertNull(event.eventId)
        } finally { NotificationNavigationEventStore.consume() }
    }

    @Test fun foregroundPushOnlyInvalidatesAndDoesNotNavigate() {
        NotificationNavigationEventStore.consume()
        val before = SeniorWithdrawalEventStore.changes.value
        SeniorWithdrawalEventStore.publish(mapOf("type" to "SOS"))
        assertEquals(before, SeniorWithdrawalEventStore.changes.value)
        SeniorWithdrawalEventStore.publish(mapOf("type" to "SENIOR_WITHDRAWAL"))
        assertEquals(before + 1, SeniorWithdrawalEventStore.changes.value)
        assertNull(NotificationNavigationEventStore.pendingEvent.value)
    }
}
