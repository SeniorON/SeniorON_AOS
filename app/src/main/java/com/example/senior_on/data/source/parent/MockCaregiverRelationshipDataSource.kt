package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockCaregiverRelationshipDataSource(
    private val activeSeniorId: Long,
    initialRelationship: CaregiverRelationship? = null,
) : CaregiverRelationshipDataSource {
    private val relationshipsBySeniorId = mutableMapOf<Long, CaregiverRelationship>()
        .apply {
            initialRelationship?.let { relationship ->
                put(activeSeniorId, relationship)
            }
        }
    private val _relationship = MutableStateFlow(initialRelationship)
    override val relationship: StateFlow<CaregiverRelationship?> =
        _relationship.asStateFlow()

    override fun saveRelationship(
        seniorId: Long,
        relationship: CaregiverRelationship,
    ) {
        require(seniorId == activeSeniorId) {
            "Relationship target does not match the active senior"
        }
        val savedRelationship = relationship.copy(
            customRelation = relationship.customRelation?.trim(),
        )
        relationshipsBySeniorId[seniorId] = savedRelationship
        _relationship.value = savedRelationship
    }

    fun relationshipFor(seniorId: Long): CaregiverRelationship? =
        relationshipsBySeniorId[seniorId]
}
