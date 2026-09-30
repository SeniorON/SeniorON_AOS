package com.example.senior_on.data.remote.websocket

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationStompSourceTest {
    private val created = """{"action":"NOTIFICATION_HOME_UPDATED","seniorId":3,"eventId":50,"notificationId":10,"type":"SOS","reason":"CREATED"}"""

    @Test fun validatesPayloadAndSelectedSeniorAndDetail() {
        val signal = requireNotNull(parseNotificationHomeSignal(created))
        assertTrue(signal.matches(3))
        assertFalse(signal.matches(4))
        assertTrue(signal.matches(3, 50))
        assertFalse(signal.matches(3, 51))
        assertTrue(NotificationHomeSignal(null).matches(3, 50))
        assertEquals("ADDRESS_UPDATED", parseNotificationHomeSignal(created.replace("CREATED", "ADDRESS_UPDATED"))?.reason)
        assertNull(parseNotificationHomeSignal("NOTIFICATION_HOME_UPDATED"))
        assertNull(parseNotificationHomeSignal(created.replace("\"seniorId\":3", "\"seniorId\":null")))
        assertNull(parseNotificationHomeSignal(created.replace("CREATED", "UNKNOWN")))
    }

    @Test fun subscribesReceivesReconnectsAndStopsOnCancellation() = runTest {
        val factory = ParentHomeStompSourceTest.FakeFactory()
        var token: String? = "Bearer first"
        val source = NotificationStompSource(factory, "https://example.test/ws", { token })
        val events = mutableListOf<NotificationHomeSignal>()
        val job = backgroundScope.launch { source.observe().collect { events += it } }
        runCurrent()
        val first = factory.sockets.single()
        first.connect(); runCurrent()
        assertTrue(first.sent.last().contains("destination:/user/queue/notification-home"))
        assertEquals(listOf(NotificationHomeSignal(null)), events)
        fun message(subscription: String, body: String) = first.listener.onMessage(first,
            "MESSAGE\nsubscription:$subscription\ndestination:/queue/notification-home-user123\n\n$body\u0000")
        message("wrong", created)
        message("notification-home", "not json")
        message("notification-home", created)
        message("notification-home", created.replace("CREATED", "ADDRESS_UPDATED"))
        runCurrent()
        assertEquals(3, events.size)
        token = "Bearer refreshed"
        first.listener.onFailure(first, IOException(), null)
        runCurrent(); advanceTimeBy(1000); runCurrent()
        assertTrue(first.cancelled)
        val second = factory.sockets.last()
        assertEquals("Bearer refreshed", second.request().header("Authorization"))
        second.connect(); runCurrent()
        assertEquals(2, events.count { it.seniorId == null })
        job.cancel(); runCurrent()
        assertTrue(second.cancelled)
        advanceTimeBy(60_000); runCurrent()
        assertEquals(2, factory.sockets.size)
    }

    @Test fun noTokenDoesNotConnectAndHandshakeTimeoutRetries() = runTest {
        val factory = ParentHomeStompSourceTest.FakeFactory()
        NotificationStompSource(factory, "https://example.test/ws", { null }).observe().collect()
        assertTrue(factory.sockets.isEmpty())
        backgroundScope.launch {
            NotificationStompSource(factory, "https://example.test/ws", { "Bearer token" }).observe().collect()
        }
        runCurrent(); advanceTimeBy(20_000); runCurrent()
        assertTrue(factory.sockets.single().cancelled)
        advanceTimeBy(1000); runCurrent()
        assertEquals(2, factory.sockets.size)
    }
}
