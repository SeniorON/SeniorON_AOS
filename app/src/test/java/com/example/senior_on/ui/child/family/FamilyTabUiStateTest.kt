package com.example.senior_on.ui.child.family

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyTabUiStateTest {
    @Test
    fun `primary caregiver can manage photo group connections`() {
        val state = FamilyTabUiState(
            members = listOf(currentUser(FamilyCaregiverRole.Primary)),
        )

        assertTrue(state.canManagePhotoGroups)
    }

    @Test
    fun `assistant caregiver cannot manage photo group connections`() {
        val state = FamilyTabUiState(
            members = listOf(currentUser(FamilyCaregiverRole.Assistant)),
        )

        assertFalse(state.canManagePhotoGroups)
    }

    private fun currentUser(role: FamilyCaregiverRole) = FamilyMemberUiModel(
        id = "1",
        name = "현재 사용자",
        role = role,
        isCurrentUser = true,
    )
}
