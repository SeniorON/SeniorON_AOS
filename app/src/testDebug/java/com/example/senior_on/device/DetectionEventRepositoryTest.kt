package com.example.senior_on.device

import com.example.senior_on.domain.repository.server.EventRepository
import java.io.IOException
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DetectionEventRepositoryTest {
    private fun real(onCall: (String) -> Nothing): EventRepository = Proxy.newProxyInstance(
        EventRepository::class.java.classLoader, arrayOf(EventRepository::class.java),
    ) { _, method, _ -> onCall(method.name) } as EventRepository

    @Test fun `감지 이벤트는 실제 서버를 호출하지 않는다`() = runTest {
        val attempts = mutableListOf<String>()
        val repository = DetectionEventRepository(real { error("Unexpected API: $it") }) { type, summary ->
            attempts += "$type $summary"
        }
        repository.createOutingReturn("OUTING", 37.123456, 127.987654, 80, "RECONNECTION", "event-1", "2026-10-25T12:00:00+09:00")
        repository.createInactivity(null, null, 80, "private-activity-time", "event-2")
        assertEquals(2, attempts.size)
        assertTrue(attempts[0].contains("phase=OUTING detectionType=RECONNECTION"))
        assertTrue(attempts[1].contains("hasLocation=false"))
        assertFalse(attempts.any { it.contains("37.123456") || it.contains("127.987654") || it.contains("private-activity-time") })
    }

    @Test fun `모의 실패가 호출자에게 전달되고 동일 이벤트로 재시도할 수 있다`() = runTest {
        val attempts = mutableListOf<String>()
        val repository = DetectionEventRepository(real { error("Unexpected API") }) { _, summary ->
            attempts += summary
            if (attempts.size == 1) throw IOException("Simulated")
        }
        try {
            repository.createInactivity(null, null, 80, "time", "same-event")
            fail("Expected failure")
        } catch (_: IOException) { }
        repository.createInactivity(null, null, 80, "time", "same-event")
        assertEquals(attempts[0], attempts[1])
    }

    @Test fun `SOS는 모의 전송으로 대체하지 않는다`() = runTest {
        var called: String? = null
        val repository = DetectionEventRepository(real { called = it; throw IllegalStateException("Real transport") }) { _, _ ->
            error("SOS must not be simulated")
        }
        try { repository.createSos(null, null, 80, "sos"); fail("Expected real transport") }
        catch (_: IllegalStateException) { }
        assertEquals("createSos", called)
    }
}
