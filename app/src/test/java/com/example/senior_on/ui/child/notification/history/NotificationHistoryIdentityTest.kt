package com.example.senior_on.ui.child.notification.history

import com.example.senior_on.ui.child.notification.*
import org.junit.Assert.*
import org.junit.Test

class NotificationHistoryIdentityTest {
    private fun message(id: Long?) = NotificationMessageUiState(
        time = "2026-09-08", title = "도움이 필요해요",
        severity = NotificationSeverity.Danger, occurredAtMillis = 1788844939557,
        notificationId = id, eventId = 7,
    )

    @Test fun sameTimeAndTitleWithDifferentIdsRemainUnique() {
        val messages = listOf(message(1), message(2)).distinctHistoryNotifications()
        assertEquals(2, messages.size)
        assertNotEquals(messages[0].historyItemKey("2026-09-08", 0), messages[1].historyItemKey("2026-09-08", 1))
    }

    @Test fun duplicateIdAcrossDatesIsKeptOnlyOnce() {
        val first = message(1)
        val messages = listOf(first, first.copy(occurredAtMillis = 1788844939557 + 86400000))
            .distinctHistoryNotifications()
        assertEquals(listOf(first), messages)
    }

    @Test fun missingAndInvalidIdsArePreservedWithUniqueKeys() {
        val messages = listOf(message(null), message(null), message(0), message(-1), message(1))
            .distinctHistoryNotifications()
        val keys = messages.mapIndexed { index, message -> message.historyItemKey("2026-09-08", index) }
        assertEquals(5, messages.size)
        assertEquals(5, keys.toSet().size)
        assertNotEquals(message(null).historyItemKey("2026-09-08", 0), message(null).historyItemKey("2026-09-09", 0))
    }

    @Test fun serverIdKeySurvivesReorderingAndReadChanges() {
        assertEquals(message(1).historyItemKey("2026-09-08", 0), message(1).copy(isRead = true).historyItemKey("2026-09-08", 3))
    }
}
