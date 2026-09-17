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
}
