package com.example.senior_on.device

import com.example.senior_on.data.remote.dto.DeviceStatusPatchRequest
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class DeviceStatusChangesTest {
    @Test fun recoveryIncludesNetworkButRepeatedCallbacksDoNot() {
        val changes = DeviceStatusChanges()
        assertEquals(NETWORK_RECOVERED, changes.observe(50, false, true, true))
        assertEquals(0, changes.observe(50, false, true, true))
    }

    @Test fun changesCanBeCombined() {
        val changes = DeviceStatusChanges()
        changes.observe(50, false, true, true)
        assertEquals(BATTERY_CHANGED or CHARGING_CHANGED, changes.observe(51, true, true, true))
        assertEquals(CHARGING_CHANGED, changes.observe(51, false, true, true))
    }

    @Test fun offlineChangesAreDiscardedAndRecoveryRequestsLatestSnapshot() {
        val changes = DeviceStatusChanges()
        changes.observe(50, false, true, true)
        assertEquals(0, changes.observe(49, false, false, true))
        assertEquals(0, changes.observe(48, true, false, true))
        assertEquals(NETWORK_RECOVERED, changes.observe(48, true, true, true))
    }

    @Test fun gpsTogglesAreDetectedButRepeatedCallbacksAreNot() {
        val changes = DeviceStatusChanges()
        changes.observe(50, false, true, true)
        assertEquals(GPS_CHANGED, changes.observe(50, false, true, false))
        assertEquals(0, changes.observe(50, false, true, false))
        assertEquals(GPS_CHANGED, changes.observe(50, false, true, true))
    }

    @Test fun partialJsonOmitsUnspecifiedFieldsButKeepsFalseAndZero() {
        val gson = Gson()
        val battery = gson.toJsonTree(DeviceStatusPatchRequest("test", batteryLevel = 0)).asJsonObject
        assertEquals(setOf("deviceIdentifier", "batteryLevel"), battery.keySet())
        assertEquals(0, battery["batteryLevel"].asInt)
        val charging = gson.toJsonTree(DeviceStatusPatchRequest("test", charging = false)).asJsonObject
        assertEquals(setOf("deviceIdentifier", "charging"), charging.keySet())
        assertFalse(charging["charging"].asBoolean)
    }
}
