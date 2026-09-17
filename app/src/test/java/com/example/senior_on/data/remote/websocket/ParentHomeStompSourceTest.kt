package com.example.senior_on.data.remote.websocket

import com.example.senior_on.domain.repository.parent.ParentHomeUpdateEvent
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import okhttp3.*
import okio.ByteString
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentHomeStompSourceTest {
    @Test fun subscribesToOwnTopicAndReconcilesOnEachConnection() = runTest {
        val factory = FakeFactory()
        var token: String? = "Bearer first"
        val logs = mutableListOf<String>()
        val source = ParentHomeStompSource(factory, "https://example.test/ws", log = { logs += it }) { token }
        val events = mutableListOf<ParentHomeUpdateEvent>()
        val job = backgroundScope.launch { source.observe(42).collect { events += it } }
        runCurrent()
        val first = factory.sockets.single()
        assertEquals("Bearer first", first.request().header("Authorization"))
        first.connect()
        runCurrent()
        assertTrue(first.sent.first().startsWith("CONNECT\n"))
        assertTrue(first.sent.last().contains("destination:/topic/senior/42/home"))
        assertEquals(1, events.count { it == ParentHomeUpdateEvent.Subscribed })

        first.message("/topic/senior/99/home")
        first.message("/topic/senior/42/home")
        runCurrent()
        assertEquals(1, events.count { it == ParentHomeUpdateEvent.HomeUpdated })

        token = "Bearer refreshed"
        first.listener.onFailure(first, IOException(), null)
        runCurrent()
        assertTrue(first.cancelled)
        advanceTimeBy(1_000)
        runCurrent()
        val second = factory.sockets.last()
        assertEquals("Bearer refreshed", second.request().header("Authorization"))
        second.connect()
        runCurrent()
        assertEquals(2, events.count { it == ParentHomeUpdateEvent.Subscribed })
        job.cancel()
        runCurrent()
        assertTrue(second.cancelled)
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(2, factory.sockets.size)
        assertTrue(logs.any { it.contains("WS opened: HTTP 101") })
        assertTrue(logs.any { it.contains("SUBSCRIBE queued") })
        assertTrue(logs.any { it.contains("HOME_UPDATED accepted") })
        assertTrue(logs.any { it.contains("MESSAGE ignored") })
        assertTrue(logs.any { it.contains("reconnect scheduled in 1000ms") })
        assertTrue(logs.any { it.contains("WS cleanup") })
        assertFalse(logs.any { it.contains("Bearer") || it.contains("/topic/senior/42") })
    }

    @Test fun missingTokenDoesNotOpenSocket() = runTest {
        val factory = FakeFactory()
        ParentHomeStompSource(factory, "https://example.test/ws") { null }.observe(1).collect()
        assertTrue(factory.sockets.isEmpty())
    }

    @Test fun recognizesDetailEventsOnlyForOwnSubscription() = runTest {
        val factory = FakeFactory()
        val events = mutableListOf<ParentHomeUpdateEvent>()
        backgroundScope.launch {
            ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }
                .observe(42).collect { events += it }
        }
        runCurrent()
        val socket = factory.sockets.single()
        socket.connect()
        runCurrent()
        fun send(body: String, topic: String = "/topic/senior/42/home", subscription: String = "senior-home") {
            socket.listener.onMessage(socket, "MESSAGE\nsubscription:$subscription\ndestination:$topic\n\n$body\u0000")
        }
        send("SCHEDULE_UPDATED")
        send("MEDICATION_UPDATED")
        send("MEDICATION_UPDATED", topic = "/topic/senior/99/home")
        send("SCHEDULE_UPDATED", subscription = "other")
        send("UNKNOWN")
        runCurrent()
        assertEquals(1, events.count { it == ParentHomeUpdateEvent.ScheduleUpdated })
        assertEquals(1, events.count { it == ParentHomeUpdateEvent.MedicationUpdated })
    }

    @Test fun missingConnectedFrameTimesOutAndRetries() = runTest {
        val factory = FakeFactory()
        backgroundScope.launch {
            ParentHomeStompSource(factory, "https://example.test/ws") { "Bearer token" }.observe(1).collect()
        }
        runCurrent()
        advanceTimeBy(20_000)
        runCurrent()
        assertTrue(factory.sockets.first().cancelled)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, factory.sockets.size)
    }

    internal class FakeFactory : WebSocket.Factory {
        val sockets = mutableListOf<FakeSocket>()
        override fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket =
            FakeSocket(request, listener).also { sockets += it }
    }

    internal class FakeSocket(private val request: Request, val listener: WebSocketListener) : WebSocket {
        val sent = mutableListOf<String>()
        var cancelled = false
        override fun request() = request
        override fun queueSize() = 0L
        override fun send(text: String): Boolean { sent += text; return true }
        override fun send(bytes: ByteString) = true
        override fun close(code: Int, reason: String?) = true
        override fun cancel() { cancelled = true }
        fun connect() {
            listener.onOpen(this, Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(101).message("Switching Protocols").build())
            listener.onMessage(this, "CONNECTED\nversion:1.2\n\n\u0000")
        }
        fun message(destination: String) {
            listener.onMessage(this, "MESSAGE\nsubscription:senior-home\ndestination:$destination\ncontent-length:12\n\nHOME_UPDATED\u0000")
        }
    }
}
