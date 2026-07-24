package com.example.senior_on.ui.display

import com.example.senior_on.domain.model.SeniorHomeButtonType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DisplayButtonAddPolicyTest {
    @Test
    fun counterIncludesProvidedFeatures() {
        assertEquals(8, buttonAddSelectedCount(selectedAppCount = 5))
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
