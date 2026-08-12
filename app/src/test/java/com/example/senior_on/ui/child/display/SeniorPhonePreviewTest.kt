package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeniorPhonePreviewTest {
    @Test
    fun `actual button items preserve custom apps labels and order`() {
        val customApp = DisplayHomeButton(
            id = 10L,
            order = 0,
            name = "가족 영상통화",
            actionType = "APP",
            actionValue = "com.example.video",
            packageName = "com.example.video",
            type = null,
        )
        val renamedCall = DisplayHomeButton(
            id = 11L,
            order = 1,
            name = "아들에게 전화",
            actionType = "DEFAULT",
            actionValue = "CALL",
            type = SeniorHomeButtonType.Call,
        )

        val result = resolvePreviewButtons(
            configuration = SeniorScreenConfiguration(),
            buttonItems = listOf(customApp, renamedCall),
        )

        assertEquals(listOf("가족 영상통화", "아들에게 전화"), result.map { it.label })
        assertNull(result.first().type)
        assertEquals("com.example.video", result.first().packageName)
        assertEquals(SeniorHomeButtonType.Call, result.last().type)
    }

    @Test
    fun `configuration is used as fallback when actual items are empty`() {
        val configuration = SeniorScreenConfiguration(
            buttons = listOf(
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Call,
            ),
            customButtonLabels = mapOf(
                SeniorHomeButtonType.Message to "딸에게 문자",
            ),
        )

        val result = resolvePreviewButtons(
            configuration = configuration,
            buttonItems = emptyList(),
        )

        assertEquals(listOf("딸에게 문자", "전화"), result.map { it.label })
        assertEquals(
            listOf(SeniorHomeButtonType.Message, SeniorHomeButtonType.Call),
            result.map { it.type },
        )
    }
}
