package com.example.senior_on.location.tracking

import com.example.senior_on.data.remote.dto.OutingReturnRequest
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class OutingObservationPolicyTest {
    private fun baseline(state: OutingTrackingState, reconnect: Boolean = false) = OutingSnapshot(
        account = "parent:1", home = StoredHomeLocation(37.0, 127.0), state = state,
        deliveredState = state, needsRecheck = reconnect,
    )
    private fun observe(old: OutingSnapshot, next: OutingTrackingState, at: Long = 100) =
        applyOutingObservation(old, next, 37.1, 127.1, at)

    @Test fun `online home to outside is a transition`() {
        val result = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        assertEquals(OutingTrackingState.Outing, result.state)
        assertEquals(OutingDetectionType.TRANSITION, result.pending?.detectionType)
        assertEquals(OutingTrackingState.Home, result.deliveredState)
    }
    @Test fun `online outside to home is a transition`() {
        val result = observe(baseline(OutingTrackingState.Outing), OutingTrackingState.Home)
        assertEquals(OutingTrackingState.Home, result.pending?.state)
        assertEquals(OutingDetectionType.TRANSITION, result.pending?.detectionType)
    }
    @Test fun `reconnection outside uses confirmed time not guessed departure time`() {
        val result = observe(baseline(OutingTrackingState.Home, true), OutingTrackingState.Outing, 900)
        assertEquals(OutingDetectionType.RECONNECTION, result.pending?.detectionType)
        assertEquals(900L, result.pending?.confirmedAt)
        assertFalse(result.needsRecheck)
    }
    @Test fun `reconnection home uses confirmation type`() {
        val result = observe(baseline(OutingTrackingState.Outing, true), OutingTrackingState.Home)
        assertEquals(OutingDetectionType.RECONNECTION, result.pending?.detectionType)
    }
    @Test fun `same state after reconnect creates no event`() {
        for (state in listOf(OutingTrackingState.Home, OutingTrackingState.Outing)) {
            assertNull(observe(baseline(state, true), state).pending)
        }
    }
    @Test fun `first observation establishes silent baseline`() {
        val result = observe(baseline(OutingTrackingState.Unknown, true), OutingTrackingState.Outing)
        assertNull(result.pending)
        assertEquals(OutingTrackingState.Outing, result.deliveredState)
    }
    @Test fun `failed delivery retains one pending event without reverting observation`() {
        val failed = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        val retry = observe(failed, OutingTrackingState.Outing, 200)
        assertEquals(failed.pending?.id, retry.pending?.id)
        assertEquals(OutingTrackingState.Outing, retry.state)
        assertEquals(OutingTrackingState.Home, retry.deliveredState)
    }
    @Test fun `resolved pending outing is cancelled rather than sent with a return`() {
        val failed = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        val returned = observe(failed.copy(needsRecheck = true), OutingTrackingState.Home)
        assertNull(returned.pending)
        assertEquals(OutingTrackingState.Home, returned.state)
    }
    @Test fun `reconfirmed pending event preserves original payload after offline gap`() {
        val failed = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        val result = applyOutingObservation(failed.copy(needsRecheck = true),
            OutingTrackingState.Outing, 38.0, 128.0, 900)
        assertEquals(failed.pending, result.pending)
        assertEquals(900L, result.confirmedAt)
        assertEquals(38.0, result.latitude)
    }
    @Test fun `uncertain boundary and invalid distances do not change state`() {
        assertNull(observedOutingState(900f))
        assertNull(observedOutingState(Float.NaN))
        assertEquals(OutingTrackingState.Home, observedOutingState(800f))
        assertEquals(OutingTrackingState.Outing, observedOutingState(1000f))
    }
    @Test fun `snapshot round trip keeps observation and pending delivery separate`() {
        val snapshot = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        val gson = Gson()
        assertEquals(snapshot, gson.fromJson(gson.toJson(snapshot), OutingSnapshot::class.java))
    }
    @Test fun `request includes backend identity and offset timestamp`() {
        val pending = PendingOutingEvent(state = OutingTrackingState.Home,
            detectionType = OutingDetectionType.RECONNECTION, confirmedAt = 0,
            latitude = 37.0, longitude = 127.0)
        val json = Gson().toJsonTree(OutingReturnRequest("RETURN", 37.0, 127.0, 50,
            "RECONNECTION", pending.id, pending.detectedAtText())).asJsonObject
        assertEquals("RECONNECTION", json["detectionType"].asString)
        assertEquals("RETURN", json["phase"].asString)
        assertEquals(pending.id, json["clientEventId"].asString)
        assertEquals("1970-01-01T09:00:00+09:00", json["detectedAt"].asString)
    }

    @Test fun `next transition after successful delivery creates new identity`() {
        val first = observe(baseline(OutingTrackingState.Home), OutingTrackingState.Outing)
        val delivered = first.copy(pending = null, deliveredState = OutingTrackingState.Outing)
        val next = observe(delivered, OutingTrackingState.Home, 200)
        assertNotEquals(first.pending!!.id, next.pending!!.id)
        assertEquals(200L, next.pending.confirmedAt)
    }
}
