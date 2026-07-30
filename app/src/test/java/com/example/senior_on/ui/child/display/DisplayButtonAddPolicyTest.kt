package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayButtonAddPolicyTest {
    @Test
    fun counterIncludesProvidedFeatures() {
        assertEquals(8, buttonAddSelectedCount(selectedAppCount = 5))
    }

    @Test
    fun defaultConfigurationHasTenGridButtonsIncludingEmergency() {
        val defaultButtons = SeniorScreenConfiguration().buttons
        val selectedAppCount = defaultButtons
            .count(ButtonAppCatalog::contains)

        assertTrue(SeniorHomeButtonType.Camera in defaultButtons)
        assertTrue(SeniorHomeButtonType.NaverMap in defaultButtons)
        assertEquals(9, buttonAddSelectedCount(selectedAppCount))
        assertEquals(12, buttonAddSelectedCount(selectedAppCount) + 3)
    }

    @Test
    fun continueRequiresFourAppsExcludingFiveFixedButtons() {
        assertFalse(buttonAddCanContinue(selectedAppCount = 3))
        assertTrue(buttonAddCanContinue(selectedAppCount = 4))
    }

    @Test
    fun deleteIsDisabledWhenSevenButtonsRemainExcludingEmergency() {
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 6))
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 7))
        assertTrue(canDeleteSelectedButton(selectedButtonCount = 8))
    }

    @Test
    fun musicSelectionReducesMaximumCountByOne() {
        assertEquals(16, buttonAddMaximumCount(hasMusicButton = false))
        assertEquals(15, buttonAddMaximumCount(hasMusicButton = true))
    }

    @Test
    fun baeminRemainsATypeButIsNotSelectableFromCatalog() {
        assertFalse(SeniorHomeButtonType.Baemin in ButtonAppCatalog)
        assertFalse(SeniorHomeButtonType.Photo in ButtonAppCatalog)
        assertEquals(38, ButtonAppCatalog.size)
    }
}
