package com.example.senior_on.device

import org.junit.Assert.*
import org.junit.Test

class DetectionReleaseSafetyTest {
    @Test fun releasePolicyRejectsEveryQaPreset() {
        DetectionConfig.entries.forEach { selected ->
            val actual = restrictDetectionConfig(selected, allowQa = false)
            assertSame(DetectionConfig.Production, actual)
            assertFalse(actual.qaEnabled)
            assertFalse(actual.simulateSendFailure)
            assertEquals("outing_tracking", actual.storeName("outing_tracking"))
            assertEquals(1_000f, actual.outingMeters)
            assertEquals(300_000L, actual.foregroundIntervalMillis)
        }
    }
}
