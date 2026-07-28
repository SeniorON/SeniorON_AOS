package com.example.senior_on.data.source.family

import com.example.senior_on.data.source.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.source.mock.fixtures.MockUserFixtures
import com.example.senior_on.domain.model.family.FamilyMemberRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockFamilyFixturesTest {
    @Test
    fun `child account receives primary caregiver data`() {
        val overview = MockFamilyFixtures.overviewForUserId(
            MockUserFixtures.PRIMARY_CAREGIVER_USER_ID
        )
        val currentUser = overview.members.single { it.isCurrentUser }

        assertEquals(MockFamilyFixtures.PRIMARY_MEMBER_ID, currentUser.id)
        assertEquals(FamilyMemberRole.Primary, currentUser.role)
    }

    @Test
    fun `child01 account receives assistant caregiver data`() {
        val overview = MockFamilyFixtures.overviewForUserId(
            MockUserFixtures.ASSISTANT_CAREGIVER_USER_ID
        )
        val currentUser = overview.members.single { it.isCurrentUser }

        assertEquals(MockFamilyFixtures.ASSISTANT_MEMBER_ID, currentUser.id)
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
