package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.data.repository.mock.fixtures.MockSeniorFixtures
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockCaregiverRelationshipRepositoryTest {
    private val seniorId = MockSeniorFixtures.SENIOR_ID

    @Test
    fun `보조 담당자의 관계 변경은 주 담당자의 관계를 변경하지 않는다`() {
        val primaryRepository = MockCaregiverRelationshipRepository(
            activeSeniorId = seniorId,
            initialRelationship = CaregiverRelationship(
                relation = SeniorRelationType.MOTHER,
            ),
        )
        val assistantRepository = MockCaregiverRelationshipRepository(
            activeSeniorId = seniorId,
        )

        assistantRepository.saveRelationship(
            seniorId = seniorId,
            relationship = CaregiverRelationship(
                relation = SeniorRelationType.OTHER,
                customRelation = "  삼촌  ",
            ),
        )

        assertEquals("어머니", primaryRepository.relationship.value?.displayLabel)
        assertEquals("삼촌", assistantRepository.relationship.value?.displayLabel)
        assertEquals(
            "삼촌",
            assistantRepository.relationshipFor(seniorId)?.displayLabel,
        )
    }

    @Test
    fun `다른 시니어 식별자에는 관계를 저장할 수 없다`() {
        val repository = MockCaregiverRelationshipRepository(
            activeSeniorId = seniorId,
        )

        val result = runCatching {
            repository.saveRelationship(
                seniorId = seniorId + 1,
                relationship = CaregiverRelationship(
                    relation = SeniorRelationType.FATHER,
                ),
            )
        }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertNull(repository.relationship.value)
    }
}
