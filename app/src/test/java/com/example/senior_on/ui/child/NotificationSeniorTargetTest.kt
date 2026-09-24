package com.example.senior_on.ui.child

import com.example.senior_on.notification.NotificationNavigationEventStore
import org.junit.Assert.*
import org.junit.After
import org.junit.Test

class NotificationSeniorTargetTest {
    @After fun cleanup() { NotificationNavigationEventStore.consume() }

    @Test fun notificationRetainsSeniorIdWithoutConvertingItToUserId() {
        NotificationNavigationEventStore.publish(mapOf("type" to "SOS", "seniorId" to "42", "eventId" to "8"))
        val event = requireNotNull(NotificationNavigationEventStore.pendingEvent.value)
        assertEquals(42L, event.seniorId)
        assertNull(event.parentUserId)
    }

    @Test fun medicationCheckedRetainsUserIdForManagedSeniorLookup() {
        NotificationNavigationEventStore.publish(mapOf("type" to "MEDICATION_CHECKED", "parentUserId" to "901", "medicationLogId" to "8"))
        val event = requireNotNull(NotificationNavigationEventStore.pendingEvent.value)
        assertNull(event.seniorId)
        assertEquals(901L, event.parentUserId)
    }
}
