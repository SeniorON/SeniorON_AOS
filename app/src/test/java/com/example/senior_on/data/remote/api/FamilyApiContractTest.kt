package com.example.senior_on.data.remote.api

import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.Query

class FamilyApiContractTest {
    @Test
    fun `family disconnect sends family id in path without device query`() {
        val method = FamilyApi::class.java.methods.single { it.name == "disconnectFamily" }
        assertEquals("api/families/{familyId}/connection", requireNotNull(method.getAnnotation(retrofit2.http.DELETE::class.java)).value)
        val paths = method.parameterAnnotations.flatten().filterIsInstance<retrofit2.http.Path>()
        assertEquals(listOf("familyId"), paths.map { it.value })
        assertEquals(emptyList<String>(), queryNames("disconnectFamily"))
    }
    @Test
    fun `family members sends the selected senior id`() {
        val method = FamilyApi::class.java.methods.single { it.name == "getMembers" }
        val queries = method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()

        assertEquals(listOf("seniorId"), queries.map(Query::value))
    }

    @Test
    fun `family code sends the selected family id`() {
        assertEquals(listOf("familyId"), queryNames("getCode"))
        val method = FamilyApi::class.java.methods.single { it.name == "getCode" }
        assertEquals(Long::class.javaPrimitiveType, method.parameterTypes[0])
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
    fun `family photo report uses the report endpoint with path id and body`() {
        val method = FamilyApi::class.java.methods.single { it.name == "reportPhoto" }

        assertEquals(
            "api/family/photos/{familyPhotoId}/reports",
            requireNotNull(method.getAnnotation(POST::class.java)).value,
        )
        assertEquals(
            listOf("familyPhotoId"),
            method.parameterAnnotations.flatten().filterIsInstance<Path>().map(Path::value),
        )
        assertEquals(1, method.parameterAnnotations.flatten().filterIsInstance<Body>().size)
    }

    @Test
    fun `family photo uploader report uses the uploader report endpoint with path id and body`() {
        val method = FamilyApi::class.java.methods.single { it.name == "reportUploader" }

        assertEquals(
            "api/family/photos/{familyPhotoId}/uploader-reports",
            requireNotNull(method.getAnnotation(POST::class.java)).value,
        )
        assertEquals(
            listOf("familyPhotoId"),
            method.parameterAnnotations.flatten().filterIsInstance<Path>().map(Path::value),
        )
        assertEquals(1, method.parameterAnnotations.flatten().filterIsInstance<Body>().size)
    }

    @Test
    fun `required family scope uses a non-null senior id`() {
        listOf(
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

        val codeMethod = FamilyApi::class.java.methods.single { it.name == "getCode" }
        val familyIdIndex = codeMethod.parameterAnnotations.indexOfFirst { annotations ->
            annotations.filterIsInstance<Query>().any { it.value == "familyId" }
        }
        assertEquals(Long::class.javaPrimitiveType, codeMethod.parameterTypes[familyIdIndex])
    }

    private fun queryNames(methodName: String): List<String> {
        val method = FamilyApi::class.java.methods.single { it.name == methodName }
        return method.parameterAnnotations
            .flatten()
            .filterIsInstance<Query>()
            .map(Query::value)
    }
}
