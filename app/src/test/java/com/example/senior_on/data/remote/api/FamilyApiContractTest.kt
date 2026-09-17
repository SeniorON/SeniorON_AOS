package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.Query

class FamilyApiContractTest {
    @Test
    fun `family members sends the selected senior id`() {
        val method = FamilyApi::class.java.methods.single { it.name == "getMembers" }
        val queries = method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()

        assertEquals(listOf("seniorId"), queries.map(Query::value))
    }
}
