package com.example.senior_on.ui.child.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeniorManagementNavigationTest {
    @Test
    fun `choice back closes the add flow`() {
        assertNull(
            resolveSeniorManagementBackDestination(
                SeniorManagementDestination.CodeChoice
            )
        )
    }

    @Test
    fun `both code branches return to choice`() {
        listOf(SeniorManagementDestination.CodeInput, SeniorManagementDestination.FamilyCodeCreated).forEach {
            assertEquals(SeniorManagementDestination.CodeChoice, resolveSeniorManagementBackDestination(it))
        }
    }

    @Test
    fun `address search back returns to the preserved parent info form`() {
        assertEquals(
            SeniorManagementDestination.ParentInfoInput,
            resolveSeniorManagementBackDestination(
                SeniorManagementDestination.AddressSearch
            )
        )
    }

    @Test
    fun `parent info back returns to generated family code`() {
        assertEquals(
            SeniorManagementDestination.FamilyCodeCreated,
            resolveSeniorManagementBackDestination(
                SeniorManagementDestination.ParentInfoInput
            ),
        )
    }
}
