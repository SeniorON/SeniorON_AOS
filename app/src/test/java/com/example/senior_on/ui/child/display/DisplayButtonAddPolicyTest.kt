package com.example.senior_on.ui.child.display

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
}
