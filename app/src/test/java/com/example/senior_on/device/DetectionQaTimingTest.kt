package com.example.senior_on.device

import org.junit.Assert.*
import org.junit.Test

class DetectionQaTimingTest {
    @Test fun `QA는 서버 시간 설정 대신 2분을 쓰고 운영은 서버 설정을 따른다`() {
        assertEquals(120_000L, DetectionConfig.Qa.inactivityThresholdMillis(4))
        assertEquals(14_400_000L, DetectionConfig.Production.inactivityThresholdMillis(4))
        assertEquals(30_000L, DetectionConfig.Qa.foregroundIntervalMillis)
        assertEquals(300_000L, DetectionConfig.Production.foregroundIntervalMillis)
    }

    @Test fun `2분 경계에서만 감지하고 비활성화와 중복 차단은 유지한다`() {
        val state = ParentInactivityState(1000L, 4, true, null)
        assertFalse(ParentInactivityPolicy.shouldCreateEvent(state, 120_999L, 120_000L))
        assertTrue(ParentInactivityPolicy.shouldCreateEvent(state, 121_000L, 120_000L))
        assertFalse(ParentInactivityPolicy.shouldCreateEvent(state.copy(enabled = false), 121_000L, 120_000L))
        assertFalse(ParentInactivityPolicy.shouldCreateEvent(state.copy(alertedForActivityAtMillis = 1000L), 121_000L, 120_000L))
    }
}
