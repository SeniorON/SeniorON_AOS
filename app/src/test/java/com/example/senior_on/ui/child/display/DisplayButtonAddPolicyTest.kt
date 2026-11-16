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
                SeniorHomeButtonType.Emergency,
                SeniorHomeButtonType.Photo,
                SeniorHomeButtonType.Schedule,
            ),
            ProvidedFeatureButtons,
        )
    }

    @Test
    fun twoInstalledMusicAppsCanBeSelectedAndRemovedIndependently() {
        val spotify = DisplayHomeButton(
            name = "Spotify",
            actionType = "APP",
            actionValue = "com.spotify.music",
            packageName = "com.spotify.music",
            type = SeniorHomeButtonType.Spotify,
        )
        val melon = DisplayHomeButton(
            name = "Melon",
            actionType = "APP",
            actionValue = "com.iloen.melon",
            packageName = "com.iloen.melon",
        )
        val draft = createInitialButtonOrder(listOf(spotify), listOf(spotify, melon))
        assertEquals(listOf(spotify, melon), draft.filter { it.actionType == "APP" })

        val edited = mergeSelectedButtonEdits(draft, listOf(melon))
        assertEquals(listOf(melon), edited.filter { it.actionType == "APP" })
    }

    @Test
    fun counterKeepsAllFourRequiredGeneralButtons() {
        assertEquals(9, buttonAddSelectedCount(selectedAppCount = 5))
    }

    @Test
    fun continueRequiresFourAppsInAdditionToFourRequiredGeneralButtons() {
        assertEquals(4, buttonAddMinimumAppCount())
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
        assertEquals(18, buttonAddSelectedCount(selectedAppCount = 14))
        assertEquals(19, buttonAddSelectedCount(selectedAppCount = 15))
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
