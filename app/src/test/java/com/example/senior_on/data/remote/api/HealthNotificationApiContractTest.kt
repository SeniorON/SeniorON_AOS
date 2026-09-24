package com.example.senior_on.data.remote.api

import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.*

class HealthNotificationApiContractTest {
    @Test fun pathParametersPrecedeQueries() {
        listOf(NotificationApi::class.java, HospitalApi::class.java, MedicationApi::class.java).forEach { api ->
            api.methods.forEach { method ->
                var seenQuery = false
                method.parameterAnnotations.forEach { annotations ->
                    if (annotations.any { it is Query || it is QueryMap || it is QueryName }) seenQuery = true
                    if (annotations.any { it is Path }) {
                        assertFalse("${api.simpleName}.${method.name}: @Path must precede @Query", seenQuery)
                    }
                }
            }
        }
    }

    @Test fun healthEndpointsUseSeniorPathsNotParentUserIds() {
        listOf(HospitalApi::class.java, MedicationApi::class.java).forEach { api ->
            api.methods.forEach { method ->
                val path = method.getAnnotation(GET::class.java)?.value
                    ?: method.getAnnotation(POST::class.java)?.value
                    ?: method.getAnnotation(PUT::class.java)?.value
                    ?: method.getAnnotation(DELETE::class.java)?.value ?: return@forEach
                assertFalse(path, path.contains("parents/"))
                if (api == HospitalApi::class.java || method.name in setOf("getMedications", "create", "update", "delete", "getParentSchedules", "getParentMonthlySchedules")) {
                    assertTrue(path, path.contains("seniors/{seniorId}"))
                    assertTrue(method.parameterAnnotations.flatten().filterIsInstance<Path>().any { it.value == "seniorId" })
                }
            }
        }
    }

    @Test fun onlyScopedNotificationEndpointsRequireSeniorQuery() {
        val scoped = setOf("getNotifications", "getSettings", "updateSetting", "getParentDeviceStatus")
        NotificationApi::class.java.methods.forEach { method ->
            val queries = method.parameterAnnotations.flatten().filterIsInstance<Query>().map { it.value }
            assertEquals(method.name, method.name in scoped, "seniorId" in queries)
        }
        val inactivity = NotificationApi::class.java.methods.single { it.name == "updateInactivitySetting" }
        assertEquals(listOf("targetUserId"), inactivity.parameterAnnotations.flatten().filterIsInstance<Path>().map { it.value })
    }
}
