package com.example.senior_on.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FamilyCodeDtosTest {
    private val gson = Gson()

    @Test
    fun joinRequestUsesSeniorCodeOnly() {
        val json = gson.toJsonTree(FamilyJoinRequest("ABCD1234")).asJsonObject

        assertEquals("ABCD1234", json.get("seniorCode").asString)
        assertFalse(json.has("familyCode"))
    }

    @Test
    fun joinResponseReadsSeniorCodeAndFamilyId() {
        val response = gson.fromJson(
            """{"familyId":7,"seniorCode":"ABCD1234"}""",
            FamilyJoinResponse::class.java,
        )

        assertEquals(FamilyJoinResponse(7L, "ABCD1234"), response)
    }

    @Test
    fun createResponseReadsSeniorCodeAndFamilyId() {
        val response = gson.fromJson(
            """{"familyId":7,"seniorCode":"ABCD1234"}""",
            FamilyCodeCreateResponse::class.java,
        )

        assertEquals(FamilyCodeCreateResponse(7L, "ABCD1234"), response)
    }

    @Test
    fun codeResponseReadsSeniorCodeAndMemberCount() {
        val response = gson.fromJson(
            """{"seniorCode":"ABCD1234","familyMemberCount":3}""",
            FamilyCodeResponse::class.java,
        )

        assertEquals(FamilyCodeResponse("ABCD1234", 3L), response)
    }
}
