package com.example.senior_on.data.repository.mock.fixtures

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockFixtureConsistencyTest {
    @Test
    fun `login accounts use the shared user profiles`() {
        assertEquals(
            MockUserFixtures.loginProfiles,
            MockAuthFixtures.loginAccounts.map { account -> account.profile },
        )
    }

    @Test
    fun `family members and photo authors use shared caregiver names`() {
        val overview = MockFamilyFixtures.primaryCaregiverOverview
        val caregiverNames = MockUserFixtures.familyCaregiverProfiles
            .map { profile -> profile.name }
            .toSet()

        assertEquals(caregiverNames, overview.members.map { member -> member.name }.toSet())
        assertTrue(
            overview.sharedPhotos.all { photo ->
                photo.authorName in caregiverNames
            }
        )
    }

    @Test
    fun `senior and parent fixtures describe the same person`() {
        assertEquals(MockUserFixtures.senior.name, MockSeniorFixtures.mother.name)
        assertEquals(MockSeniorFixtures.SENIOR_ID, MockSeniorFixtures.mother.seniorId)
    }

    @Test
    fun `family photos use shared family member identities`() {
        val photos = MockFamilyPhotoFixtures.initialPhotos(Instant.EPOCH)

        assertEquals(13, photos.size)
        assertEquals(
            setOf(
                MockFamilyFixtures.PRIMARY_MEMBER_ID,
                MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID,
            ),
            photos.map { photo -> photo.authorMemberId }.toSet(),
        )
        assertEquals(
            photos.size,
            photos.map { photo -> photo.id }.toSet().size,
        )
    }
}
