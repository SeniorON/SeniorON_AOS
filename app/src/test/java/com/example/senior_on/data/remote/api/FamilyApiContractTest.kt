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

    @Test
    fun `family code sends the selected senior id`() {
        assertEquals(listOf("seniorId"), queryNames("getCode"))
    }

    @Test
    fun `family home sends the selected senior id`() {
        assertEquals(listOf("seniorId"), queryNames("getHome"))
    }

    @Test
    fun `family photo list sends the selected senior id`() {
        assertEquals(
            listOf("uploaderUserId", "cursorCreatedAt", "cursorId", "size", "seniorId"),
            queryNames("getPhotos"),
        )
    }

    @Test
    fun `photo group connections are scoped by the selected senior`() {
        assertEquals(listOf("seniorId"), queryNames("getPhotoGroupConnections"))
        assertEquals(listOf("seniorId"), queryNames("disconnectPhotoGroup"))
    }

    @Test
    fun `family member mutations are scoped by the selected senior`() {
        assertEquals(listOf("seniorId"), queryNames("changePrimaryManager"))
        assertEquals(listOf("seniorId"), queryNames("deleteMember"))
    }

    private fun queryNames(methodName: String): List<String> {
        val method = FamilyApi::class.java.methods.single { it.name == methodName }
        return method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()
            .map(Query::value)
    }
}
