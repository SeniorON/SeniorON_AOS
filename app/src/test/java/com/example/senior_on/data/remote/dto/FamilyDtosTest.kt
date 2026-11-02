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
    fun `family photo reads the thumbnail URL separately from the original`() {
        val response = gson.fromJson(
            """{"familyPhotoId":91,"imageUrl":"https://example.com/original.jpg","thumbnailUrl":"https://example.com/thumbnail.jpg"}""",
            FamilyPhotoItemResponse::class.java,
        )

        assertEquals("https://example.com/original.jpg", response.imageUrl)
        assertEquals("https://example.com/thumbnail.jpg", response.thumbnailUrl)
    }

    @Test
    fun `photo completion sends selected senior ids`() {
        val json = gson.toJsonTree(
            FamilyPhotoUploadCompleteRequest(
                seniorId = 7L,
                seniorIds = listOf(31L, 32L),
                imageKey = "family/photo.jpg",
                description = "함께 본 사진",
            )
        ).asJsonObject

        assertEquals(7L, json["seniorId"].asLong)
        assertEquals(listOf(31L, 32L), json["seniorIds"].asJsonArray.map { it.asLong })
    }
}
