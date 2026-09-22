package com.example.senior_on.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FamilyDtosTest {
    private val gson = Gson()

    @Test
    fun `family join sends the backend seniorCode field`() {
        val json = gson.toJsonTree(
            FamilyJoinRequest(familyCode = "43TS-6GTE"),
        ).asJsonObject

        assertEquals("43TS-6GTE", json["seniorCode"].asString)
        assertFalse(json.has("familyCode"))
    }

    @Test
    fun `family creation reads familyId and seniorCode`() {
        val response = gson.fromJson(
            """{"familyId":31,"seniorCode":"43TS-6GTE"}""",
            FamilyCodeCreateResponse::class.java,
        )

        assertEquals(31L, response.familyId)
        assertEquals("43TS-6GTE", response.familyCode)
    }

    @Test
    fun `photo group connection sends selected senior and entered code`() {
        val json = gson.toJsonTree(
            FamilyPhotoGroupConnectionRequest(
                seniorId = 7L,
                seniorCode = "43TS-6GTE",
            )
        ).asJsonObject

        assertEquals(7L, json["seniorId"].asLong)
        assertEquals("43TS-6GTE", json["seniorCode"].asString)
    }

    @Test
    fun `family home reads the default photo group id`() {
        val response = gson.fromJson(
            """{"members":[],"recentPhotos":[],"photoGroupId":31}""",
            FamilyHomeResponse::class.java,
        )

        assertEquals(31L, response.photoGroupId)
    }

    @Test
    fun `photo completion sends selected photo groups`() {
        val json = gson.toJsonTree(
            FamilyPhotoUploadCompleteRequest(
                seniorId = 7L,
                photoGroupIds = listOf(31L, 32L),
                imageKey = "family/photo.jpg",
                description = "함께 본 사진",
            )
        ).asJsonObject

        assertEquals(7L, json["seniorId"].asLong)
        assertEquals(listOf(31L, 32L), json["photoGroupIds"].asJsonArray.map { it.asLong })
    }
}
