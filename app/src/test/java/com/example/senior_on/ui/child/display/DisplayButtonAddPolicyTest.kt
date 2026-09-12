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
    fun importedSupportedMusicAppTargetsTheTopMusicSection() {
        val melon = DisplayHomeButton(
            name = "멜론",
            actionType = "DEFAULT",
            actionValue = "MELON",
            type = SeniorHomeButtonType.Melon,
        )

        assertEquals(
            SeniorHomeButtonType.Melon,
            melon.importedMusicButtonTypeOrNull(),
        )
    }

    @Test
    fun importedUnknownAppStaysInTheGeneralAppSection() {
        val unknownMusicApp = DisplayHomeButton(
            name = "새 음악 앱",
            actionType = "APP",
            actionValue = "com.example.music",
            packageName = "com.example.music",
            type = null,
        )

        assertEquals(null, unknownMusicApp.importedMusicButtonTypeOrNull())
    }

    @Test
    fun importedMusicSelectionReplacesThePreviousTopMusicApp() {
        val spotify = DisplayHomeButton(
            name = "스포티파이",
            actionType = "DEFAULT",
            actionValue = "SPOTIFY",
            type = SeniorHomeButtonType.Spotify,
        )

        val result = createInitialButtonOrder(
            currentButtons = listOf(spotify),
            musicButton = SeniorHomeButtonType.Melon,
            appButtons = emptyList(),
        )

        assertEquals(
            listOf(SeniorHomeButtonType.Melon),
            result.mapNotNull(DisplayHomeButton::type)
                .filter(SeniorHomeButtonType::isMusicButton),
        )
    }

    @Test
    fun counterKeepsAllFourRequiredGeneralButtons() {
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
