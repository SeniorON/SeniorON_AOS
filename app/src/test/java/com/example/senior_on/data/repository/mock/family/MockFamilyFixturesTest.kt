package com.example.senior_on.data.repository.mock.family

import com.example.senior_on.domain.model.family.FamilyMemberRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockFamilyFixturesTest {
    @Test
    fun `child account receives primary caregiver data`() {
        val overview = MockFamilyFixtures.overviewForUserId("child")
        val currentUser = overview.members.single { it.isCurrentUser }

        assertEquals("family-member-primary", currentUser.id)
        assertEquals(FamilyMemberRole.Primary, currentUser.role)
    }

    @Test
    fun `child01 account receives assistant caregiver data`() {
        val overview = MockFamilyFixtures.overviewForUserId("child01")
        val currentUser = overview.members.single { it.isCurrentUser }

        assertEquals("family-member-assistant-1", currentUser.id)
        assertEquals(FamilyMemberRole.Assistant, currentUser.role)
        assertTrue(
            overview.sharedPhotos
                .filter { it.authorName == currentUser.name }
                .all { it.isOwnedByCurrentUser }
        )
        assertFalse(
            overview.sharedPhotos
                .filter { it.authorName != currentUser.name }
                .any { it.isOwnedByCurrentUser }
        )
    }
}
