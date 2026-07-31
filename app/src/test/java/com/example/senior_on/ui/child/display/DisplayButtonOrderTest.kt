package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayButtonOrderTest {
    @Test
    fun defaultButtonsMatchBackendInitialOrder() {
        assertEquals(
            listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.Photo,
                SeniorHomeButtonType.YouTube,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Emergency,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.Naver,
            ),
            SeniorScreenConfiguration().buttons
                .filterNot { button ->
                    button.isMusicButton() ||
                        button == SeniorHomeButtonType.Schedule
                },
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
                SeniorHomeButtonType.Photo,
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
                SeniorHomeButtonType.Photo,
            ),
            result,
        )
    }

    @Test
    fun requiredButtonsCannotBeRemovedAndEmergencyUsesEighthGridSlot() {
        val result = listOf(
            SeniorHomeButtonType.Melon,
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Emergency,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Camera,
            SeniorHomeButtonType.YouTube,
            SeniorHomeButtonType.KakaoTalk,
            SeniorHomeButtonType.Naver,
            SeniorHomeButtonType.Emergency,
        ).withRequiredSeniorHomeButtons()

        assertEquals(
            listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Schedule,
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.YouTube,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.Naver,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Emergency,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Photo,
            ),
            result,
        )
    }

    @Test
    fun emergencyOccupiesTheEighthGridSlot() {
        val regularButtons = listOf(
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Camera,
            SeniorHomeButtonType.ChatBuddy,
            SeniorHomeButtonType.Medication,
            SeniorHomeButtonType.YouTube,
            SeniorHomeButtonType.Photo,
            SeniorHomeButtonType.NaverMap,
            SeniorHomeButtonType.Naver,
        )

        val result = regularButtons.withEmergencyAtFixedGridSlot()

        assertEquals(SeniorHomeButtonType.Emergency, result[7])
        assertEquals(regularButtons, result.filterNot {
            it == SeniorHomeButtonType.Emergency
        })
    }

    @Test
    fun emergencyStaysInTheEighthGridSlotWhenOtherButtonsMove() {
        val movedButtons = listOf(
            SeniorHomeButtonType.NaverMap,
            SeniorHomeButtonType.Call,
            SeniorHomeButtonType.Message,
            SeniorHomeButtonType.Camera,
            SeniorHomeButtonType.ChatBuddy,
            SeniorHomeButtonType.Medication,
            SeniorHomeButtonType.YouTube,
            SeniorHomeButtonType.Photo,
            SeniorHomeButtonType.Naver,
            SeniorHomeButtonType.Emergency,
        )

        val result = movedButtons.withEmergencyAtFixedGridSlot()

        assertEquals(SeniorHomeButtonType.Emergency, result[7])
    }

    @Test
    fun seniorPhonePreviewAlsoUsesTheEighthEmergencyGridSlot() {
        val result = SeniorScreenConfiguration().buttons
            .filterNot { button ->
                button.isMusicButton() ||
                    button == SeniorHomeButtonType.Schedule
            }
            .withEmergencyAtFixedGridSlot()

        assertEquals(SeniorHomeButtonType.Emergency, result[7])
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
