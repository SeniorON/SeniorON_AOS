package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.Body
import retrofit2.http.Query

class HomeApiContractTest {
    @Test
    fun childHomeReadEndpointsSendSeniorIdAsQuery() {
        val childReadEndpointNames = setOf(
            "getHome",
            "getTodayHospitals",
            "getDevice",
            "getButtonOptions",
        )

        childReadEndpointNames.forEach { methodName ->
            val method = HomeApi::class.java.methods.single { it.name == methodName }
            val queries = method.parameterAnnotations
                .flatten()
                .filterIsInstance<Query>()

            assertEquals(methodName, listOf("seniorId"), queries.map(Query::value))
        }
    }

    @Test
    fun childHomeMutationEndpointsSendSeniorIdInRequestBodyOnly() {
        val childMutationEndpointNames = setOf(
            "saveButtons",
            "updateFontSize",
            "updateSeniorProfile",
        )

        childMutationEndpointNames.forEach { methodName ->
            val method = HomeApi::class.java.methods.single { it.name == methodName }
            val annotations = method.parameterAnnotations.flatten()
            val requestParameterCount = method.parameterTypes.count { parameterType ->
                parameterType.name != "kotlin.coroutines.Continuation"
            }

            assertTrue(methodName, annotations.filterIsInstance<Query>().isEmpty())
            assertEquals(methodName, 1, annotations.filterIsInstance<Body>().size)
            assertEquals(methodName, 1, requestParameterCount)
        }
    }

    @Test
    fun seniorHomeEndpointDoesNotSendTargetSeniorId() {
        val method = HomeApi::class.java.methods.single { it.name == "getSeniorHome" }

        assertTrue(
            method.parameterAnnotations
                .flatten()
                .filterIsInstance<Query>()
                .isEmpty()
        )
    }

    @Test
    fun removedWeatherAndIndividualButtonEndpointsAreNotExposed() {
        val methodNames = HomeApi::class.java.methods.map { it.name }.toSet()

        assertFalse("getWeather" in methodNames)
        assertFalse("addButton" in methodNames)
        assertFalse("updateButtons" in methodNames)
        assertFalse("deleteButton" in methodNames)
    }
}
