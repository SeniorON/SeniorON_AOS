package com.example.senior_on.di

import com.example.senior_on.device.DetectionConfig
import org.junit.Assert.*
import org.junit.Test

class DetectionBuildPolicyTest {
    @Test fun debugHonorsExplicitSelectionRatherThanEnablingQaAutomatically() {
        DetectionConfig.entries.forEach { assertSame(it, enforceDetectionBuildPolicy(it)) }
        assertFalse(enforceDetectionBuildPolicy(DetectionConfig.Production).qaEnabled)
        assertEquals(1_000f, enforceDetectionBuildPolicy(DetectionConfig.Production).outingMeters)
    }

    @Test fun qaStorageIsIsolatedAndFailureModeKeepsSameQaQueue() {
        assertEquals("outing_tracking_qa", DetectionConfig.Qa.storeName("outing_tracking"))
        assertEquals(DetectionConfig.Qa.storeName("inactivity_outbox"),
            DetectionConfig.QaSendFailure.storeName("inactivity_outbox"))
        assertTrue(DetectionConfig.QaSendFailure.simulateSendFailure)
        assertFalse(DetectionConfig.Qa.simulateSendFailure)
    }
}
