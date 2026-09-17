package com.example.senior_on.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceDtosTest {
    @Test
    fun statusUpdateSerializesEveryRequiredBackendField() {
        val request = DeviceStatusUpdateRequest(
            deviceIdentifier = "device-123",
            deviceName = "Galaxy S24",
            batteryLevel = 72,
            charging = true,
            deviceStatusSharingEnabled = true,
            networkConnected = true,
            defaultHomeEnabled = true,
            locationPermissionGranted = true,
            gpsEnabled = false,
            notificationPermissionGranted = true,
            appExecutionMaintained = false,
        )

        val json = Gson().toJsonTree(request).asJsonObject

        assertEquals("device-123", json.get("deviceIdentifier").asString)
        assertEquals("Galaxy S24", json.get("deviceName").asString)
        assertEquals(72, json.get("batteryLevel").asInt)
        assertEquals(true, json.get("charging").asBoolean)
        assertEquals(true, json.get("deviceStatusSharingEnabled").asBoolean)
        assertEquals(true, json.get("networkConnected").asBoolean)
        assertEquals(true, json.get("defaultHomeEnabled").asBoolean)
        assertEquals(true, json.get("locationPermissionGranted").asBoolean)
        assertEquals(false, json.get("gpsEnabled").asBoolean)
        assertEquals(true, json.get("notificationPermissionGranted").asBoolean)
        assertEquals(false, json.get("appExecutionMaintained").asBoolean)
    }
}
