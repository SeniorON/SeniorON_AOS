package com.example.senior_on.location.tracking

import org.junit.Assert.*
import org.junit.Test

class OutingBoundariesTest {
    @Test fun `QA는 150미터 외출 100미터 귀가 기준을 쓴다`() {
        val boundaries = outingBoundaries(qa = true)
        assertEquals(OutingTrackingState.Outing, observedOutingState(150f, boundaries))
        assertEquals(OutingTrackingState.Home, observedOutingState(100f, boundaries))
        assertNull(observedOutingState(125f, boundaries))
        assertNull(observedOutingState(Float.NaN, boundaries))
    }

    @Test fun `운영은 기존 거리 기준을 유지한다`() {
        assertEquals(OutingBoundaries(1000f, 800f), outingBoundaries(qa = false))
    }
}
