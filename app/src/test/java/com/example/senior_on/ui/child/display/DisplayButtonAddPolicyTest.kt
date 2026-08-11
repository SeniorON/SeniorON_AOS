package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayButtonAddPolicyTest {
    @Test
    fun providedFeatureSectionMatchesRequestedOrder() {
        assertEquals(
            listOf(
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Emergency,
                SeniorHomeButtonType.Photo,
                SeniorHomeButtonType.Schedule,
            ),
            ProvidedFeatureButtons,
        )
    }

    @Test
    fun musicSectionContainsAllSupportedMusicAppsInRequestedOrder() {
        assertEquals(
            listOf(
                SeniorHomeButtonType.Melon,
                SeniorHomeButtonType.Genie,
                SeniorHomeButtonType.YouTubeMusic,
                SeniorHomeButtonType.Spotify,
                SeniorHomeButtonType.Flo,
                SeniorHomeButtonType.Vibe,
                SeniorHomeButtonType.Bugs,
                SeniorHomeButtonType.SamsungMusic,
                SeniorHomeButtonType.KakaoMusic,
            ),
            MusicButtons,
        )
        assertTrue(MusicButtons.all(SeniorHomeButtonType::isMusicButton))
    }

    @Test
    fun counterIncludesAllFourRequiredGeneralButtons() {
        assertEquals(9, buttonAddSelectedCount(selectedAppCount = 5))
    }

    @Test
    fun continueRequiresFourAppsInAdditionToFourRequiredGeneralButtons() {
        assertFalse(buttonAddCanContinue(selectedAppCount = 3))
        assertTrue(buttonAddCanContinue(selectedAppCount = 4))
    }

    @Test
    fun deleteIsDisabledWhenEightGeneralButtonsRemain() {
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 7))
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 8))
        assertTrue(canDeleteSelectedButton(selectedButtonCount = 9))
    }

    @Test
    fun maximumCountsOnlyGeneralButtons() {
        assertEquals(18, buttonAddMaximumCount())
    }

    @Test
    fun cancellingAddRestoresEntryButtonsAndDropsImportedApp() {
        val phone = DisplayHomeButton(
            name = "전화",
            actionType = "DEFAULT",
            actionValue = "PHONE",
        )
        val netflix = DisplayHomeButton(
            name = "넷플릭스",
            actionType = "APP",
            actionValue = "com.netflix.mediaclient",
            packageName = "com.netflix.mediaclient",
        )

        val restoredButtons = resolveButtonEditDraftAfterButtonAdd(
            buttonsAtEntry = listOf(phone),
            selectedButtons = listOf(phone, netflix),
            exit = ButtonAddExit.Cancel,
        )

        assertEquals(listOf(phone), restoredButtons)
        assertFalse(netflix in restoredButtons)
    }

    @Test
    fun continuingAddKeepsImportedAppInDraftUntilFinalSave() {
        val phone = DisplayHomeButton(
            name = "전화",
            actionType = "DEFAULT",
            actionValue = "PHONE",
        )
        val netflix = DisplayHomeButton(
            name = "넷플릭스",
            actionType = "APP",
            actionValue = "com.netflix.mediaclient",
            packageName = "com.netflix.mediaclient",
        )

        val continuedButtons = resolveButtonEditDraftAfterButtonAdd(
            buttonsAtEntry = listOf(phone),
            selectedButtons = listOf(phone, netflix),
            exit = ButtonAddExit.Continue,
        )

        assertEquals(listOf(phone, netflix), continuedButtons)
    }
}
