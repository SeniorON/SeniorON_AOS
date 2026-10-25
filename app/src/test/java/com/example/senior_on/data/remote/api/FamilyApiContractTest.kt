package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.GET
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
    fun `photo targets use the dedicated unscoped endpoint`() {
        val method = FamilyApi::class.java.methods.single { it.name == "getPhotoTargets" }

        assertEquals(
            "api/family/photos/targets",
            requireNotNull(method.getAnnotation(GET::class.java)).value,
        )
        assertEquals(emptyList<String>(), queryNames("getPhotoTargets"))
    }

    @Test
    fun `family member mutations are scoped by the selected senior`() {
        assertEquals(listOf("seniorId"), queryNames("changePrimaryManager"))
        assertEquals(listOf("seniorId"), queryNames("deleteMember"))
    }

    @Test
    fun `required family scope uses a non-null senior id`() {
        listOf(
            "getCode",
            "getHome",
            "getMembers",
            "changePrimaryManager",
            "deleteMember",
            "getPhotos",
        ).forEach { methodName ->
            val method = FamilyApi::class.java.methods.single { it.name == methodName }
            val seniorIdIndex = method.parameterAnnotations.indexOfFirst { annotations ->
                annotations.filterIsInstance<Query>().any { it.value == "seniorId" }
            }

            assertEquals(
                "$methodName seniorId must be a required primitive long",
                Long::class.javaPrimitiveType,
                method.parameterTypes[seniorIdIndex],
            )
        }
    }

    private fun queryNames(methodName: String): List<String> {
        val method = FamilyApi::class.java.methods.single { it.name == methodName }
        return method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()
            .map(Query::value)
    }
}
