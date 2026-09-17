package com.example.senior_on.ui.child.display

import com.example.senior_on.data.source.mock.fixtures.MockSeniorFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayTopBarTitleTest {
    @Test
    fun `latest relationship state is used for the top bar`() {
        val state = DisplayTabUiState(
            parentInfo = MockSeniorFixtures.mother,
            relationshipLabel = "  아버지  ",
        )

        assertEquals("아버지", state.resolveDisplayTopBarTitle())
    }

    @Test
    fun `cached parent relationship is used while overview is loading`() {
        val state = DisplayTabUiState(parentInfo = MockSeniorFixtures.mother)

        assertEquals("어머니", state.resolveDisplayTopBarTitle())
    }

    @Test
    fun `unknown relationship leaves the loading title empty`() {
        assertNull(DisplayTabUiState().resolveDisplayTopBarTitle())
    }
}
