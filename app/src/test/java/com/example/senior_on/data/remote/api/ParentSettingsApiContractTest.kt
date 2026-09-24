package com.example.senior_on.data.remote.api

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.*

class ParentSettingsApiContractTest {
    @Test fun endpointsMatchBackend() {
        val methods = ParentSettingsApi::class.java.methods.associateBy { it.name }
        assertEquals("api/seniors/{seniorId}/permission-settings", methods.getValue("getPermissions").getAnnotation(GET::class.java)?.value)
        assertEquals("seniorId", methods.getValue("getPermissions").parameterAnnotations.flatten().filterIsInstance<Path>().single().value)
        assertEquals("api/seniors/me/permission-settings", methods.getValue("updatePermissions").getAnnotation(PATCH::class.java)?.value)
        assertEquals("api/devices/connection/me", methods.getValue("disconnect").getAnnotation(DELETE::class.java)?.value)
    }

    @Test fun partialUpdatesDoNotOverwriteOtherPermission() {
        assertEquals("{\"locationEnabled\":false}", Gson().toJson(SeniorPermissionUpdate(locationEnabled = false)))
        assertEquals("{\"inactivityDetectionEnabled\":true}", Gson().toJson(SeniorPermissionUpdate(inactivityDetectionEnabled = true)))
    }

    @Test fun responseUsesExactBackendFieldNames() {
        val value = Gson().fromJson("""{"seniorId":3,"locationEnabled":false,"inactivityDetectionEnabled":true}""", SeniorPermissionSettings::class.java)
        assertEquals(3L, value.seniorId)
        assertFalse(value.locationEnabled)
        assertTrue(value.inactivityDetectionEnabled)
    }
}
