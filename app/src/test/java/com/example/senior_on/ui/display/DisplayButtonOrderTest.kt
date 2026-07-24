package com.example.senior_on.ui.display

import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayButtonOrderTest {
    @Test
    fun defaultButtonsEndWithNaverAndEmergency() {
        assertEquals(
            listOf(
                SeniorHomeButtonType.Naver,
                SeniorHomeButtonType.Emergency,
            ),
            SeniorScreenConfiguration().buttons.takeLast(2),
        )
    }

    @Test
    fun musicAndScheduleArePlacedBeforeReorderableButtons() {
        val result = createInitialButtonOrder(
            currentButtons = listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
            ),
            musicButton = SeniorHomeButtonType.Melon,
            appButtons = listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
            ),
        )

        assertEquals(
            listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
            ),
            result,
        )
    }

    @Test
    fun scheduleIsFeaturedWhenNoMusicButtonIsSelected() {
        val result = createInitialButtonOrder(
            currentButtons = listOf(
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Call,
            ),
            musicButton = null,
            appButtons = listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Camera,
            ),
        )

        assertEquals(SeniorHomeButtonType.Schedule, result.first())
        assertEquals(
            listOf(
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
            ),
            result,
        )
    }

    @Test
    fun requiredButtonsCannotBeRemovedAndEmergencyStaysLast() {
        val result = listOf(
            SeniorHomeButtonType.Melon,
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Emergency,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Emergency,
        ).withRequiredSeniorHomeButtons()

        assertEquals(
            listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Emergency,
            ),
            result,
        )
    }

    @Test
    fun selectedButtonEditsDeleteOnlyEditableButtons() {
        val result = mergeSelectedButtonEdits(
            initialButtons = listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Emergency,
            ),
            editableButtons = listOf(
                SeniorHomeButtonType.Message,
            ),
        )

        assertEquals(
            listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Emergency,
            ),
            result,
        )
    }
}
