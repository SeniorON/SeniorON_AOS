package com.example.senior_on.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDtosTest {
    @Test
    fun buttonRequestSerializesNullPackageNameExplicitly() {
        val request = HomeButtonSaveRequest(
            seniorId = TEST_SENIOR_ID,
            musicApp = null,
            buttons = listOf(
                ButtonRequest(
                    buttonOrder = 1,
                    buttonName = "복약",
                    actionType = "DEFAULT",
                    actionValue = "MEDICATION",
                    packageName = null,
                )
            ),
        )

        val json = Gson().toJsonTree(request).asJsonObject
        val buttonJson = json
            .getAsJsonArray("buttons")
            .first()
            .asJsonObject

        assertEquals(TEST_SENIOR_ID, json.get("senior_id").asLong)
        assertFalse(json.has("seniorId"))
        assertTrue(buttonJson.has("packageName"))
        assertTrue(buttonJson.get("packageName").isJsonNull)
    }

    @Test
    fun fontSizeRequestSerializesSeniorIdInBody() {
        val request = HomeFontSizeUpdateRequest(
            seniorId = TEST_SENIOR_ID,
            font_size = "LARGE",
        )

        val json = Gson().toJsonTree(request).asJsonObject

        assertEquals(TEST_SENIOR_ID, json.get("senior_id").asLong)
        assertEquals("LARGE", json.get("font_size").asString)
        assertFalse(json.has("seniorId"))
        assertFalse(json.has("fontSize"))
    }

    @Test
    fun seniorProfileRequestSerializesAddressCoordinates() {
        val request = SeniorProfileUpdateRequest(
            seniorId = TEST_SENIOR_ID,
            name = "김영희",
            relation = "MOTHER",
            customRelation = null,
            birth = "1960-01-02",
            phoneNumber = "010-1234-5678",
            address = "서울시 강남구",
            detailAddress = "101동",
            latitude = 37.5172,
            longitude = 127.0473,
        )

        val json = Gson().toJsonTree(request).asJsonObject

        assertEquals(TEST_SENIOR_ID, json.get("senior_id").asLong)
        assertEquals(37.5172, json.get("latitude").asDouble, 0.0)
        assertEquals(127.0473, json.get("longitude").asDouble, 0.0)
        assertFalse(json.has("seniorId"))
    }
}

private const val TEST_SENIOR_ID = 3_000_000_000L
