package com.example.senior_on.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDtosTest {
    @Test
    fun buttonRequestSerializesNullPackageNameExplicitly() {
        val request = HomeButtonSaveRequest(
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

        assertTrue(!json.has("seniorId"))
        assertTrue(buttonJson.has("packageName"))
        assertTrue(buttonJson.get("packageName").isJsonNull)
    }
}
