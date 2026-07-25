package com.example.senior_on.ui.display

import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.domain.model.SeniorScreenConfiguration
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
    fun defaultConfigurationStartsAtTenSelectedButtons() {
        val defaultButtons = SeniorScreenConfiguration().buttons
        val selectedAppCount = defaultButtons
            .count(ButtonAppCatalog::contains)

        assertTrue(SeniorHomeButtonType.Camera in defaultButtons)
        assertTrue(SeniorHomeButtonType.NaverMap in defaultButtons)
        assertEquals(10, buttonAddSelectedCount(selectedAppCount))
    }

    @Test
    fun continueRequiresAtLeastSevenSelectedButtons() {
        assertFalse(buttonAddCanContinue(selectedAppCount = 3))
        assertTrue(buttonAddCanContinue(selectedAppCount = 4))
    }

    @Test
    fun deleteIsDisabledWhenSevenButtonsRemain() {
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 6))
        assertFalse(canDeleteSelectedButton(selectedButtonCount = 7))
        assertTrue(canDeleteSelectedButton(selectedButtonCount = 8))
    }

    @Test
    fun musicSelectionReducesMaximumCountByOne() {
        assertEquals(12, buttonAddMaximumCount(hasMusicButton = false))
        assertEquals(11, buttonAddMaximumCount(hasMusicButton = true))
    }

    @Test
    fun baeminRemainsATypeButIsNotSelectableFromCatalog() {
        assertFalse(SeniorHomeButtonType.Baemin in ButtonAppCatalog)
        assertEquals(39, ButtonAppCatalog.size)
    }
}
