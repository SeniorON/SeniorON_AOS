package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayButtonAddPolicyTest {
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
    fun defaultConfigurationHasTenGridButtonsIncludingEmergency() {
        val defaultButtons = SeniorScreenConfiguration().buttons
        val selectedAppCount = defaultButtons
            .count(ButtonAppCatalog::contains)

        assertTrue(SeniorHomeButtonType.Camera in defaultButtons)
        assertTrue(SeniorHomeButtonType.KakaoTalk in defaultButtons)
        assertTrue(SeniorHomeButtonType.Naver in defaultButtons)
        assertEquals(10, buttonAddSelectedCount(selectedAppCount))
        assertEquals(12, buttonAddSelectedCount(selectedAppCount) + 2)
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
    fun baeminRemainsATypeButIsNotSelectableFromCatalog() {
        assertFalse(SeniorHomeButtonType.Baemin in ButtonAppCatalog)
        assertFalse(SeniorHomeButtonType.Photo in ButtonAppCatalog)
        assertEquals(38, ButtonAppCatalog.size)
    }
}
