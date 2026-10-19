package com.example.senior_on.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParentActivityRecorderTest {
    private class Fixture {
        var loggedInParent = true
        var deviceReady = true
        var now = 100L
        var lastActivity: Long? = null
        var inputs = 0
        val recorder = ParentActivityRecorder(
            isLoggedInParent = { loggedInParent },
            isDeviceConnectionReady = { deviceReady },
            initializeIfMissing = { if (lastActivity == null) lastActivity = now },
            recordActivity = { lastActivity = now; inputs++ },
        )
    }

    @Test fun `input before family connection does not record`() {
        val f = Fixture()
        f.recorder.onUserInteraction()
        assertNull(f.lastActivity)
    }
    @Test fun `connected first entry initializes only missing timestamp`() {
        val f = Fixture()
        f.recorder.onFamilyConnectionChanged(true)
        assertEquals(100L, f.lastActivity)
        f.now = 200
        f.recorder.onFamilyConnectionChanged(false)
        f.recorder.onFamilyConnectionChanged(true)
        assertEquals(100L, f.lastActivity)
        assertEquals(0, f.inputs)
    }
    @Test fun `eligible user input updates time`() {
        val f = Fixture()
        f.recorder.onFamilyConnectionChanged(true)
        f.now = 200
        f.recorder.onUserInteraction()
        assertEquals(200L, f.lastActivity)
        assertEquals(1, f.inputs)
    }
    @Test fun `logout and disconnected gate prevent further input recording`() {
        val f = Fixture()
        f.recorder.onFamilyConnectionChanged(true)
        f.now = 200
        f.loggedInParent = false
        f.recorder.onUserInteraction()
        f.loggedInParent = true
        f.deviceReady = false
        f.recorder.onUserInteraction()
        assertEquals(100L, f.lastActivity)
        assertEquals(0, f.inputs)
    }
    @Test fun `unapproved session cannot initialize baseline`() {
        val f = Fixture()
        f.deviceReady = false
        f.recorder.onFamilyConnectionChanged(true)
        assertNull(f.lastActivity)
        f.deviceReady = true
        f.loggedInParent = false
        f.recorder.onFamilyConnectionChanged(true)
        assertNull(f.lastActivity)
    }
    @Test fun `membership lost blocks further input`() {
        val f = Fixture()
        f.recorder.onFamilyConnectionChanged(true)
        f.recorder.onFamilyConnectionChanged(false)
        f.now = 200
        f.recorder.onUserInteraction()
        assertEquals(100L, f.lastActivity)
    }
}
