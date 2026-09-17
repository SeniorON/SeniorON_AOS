package com.example.senior_on.ui.child.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeniorManagementNavigationTest {
    @Test
    fun `family code back closes the add flow`() {
        assertNull(
            resolveSeniorManagementBackDestination(
                SeniorManagementDestination.FamilyCodeCreated
            )
        )
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
