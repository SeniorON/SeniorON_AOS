package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.Query

class DeviceApiContractTest {
    @Test
    fun childDeviceEndpointsSendSelectedSeniorId() {
        listOf("disconnect", "getLatestLocation").forEach { methodName ->
            val method = DeviceApi::class.java.methods.single { it.name == methodName }
            val queries = method.parameterAnnotations
                .flatten()
                .filterIsInstance<Query>()

            assertEquals(methodName, listOf("seniorId"), queries.map(Query::value))
        }
    }
}
