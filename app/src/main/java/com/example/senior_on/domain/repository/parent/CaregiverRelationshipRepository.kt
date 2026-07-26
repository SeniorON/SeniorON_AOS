package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import kotlinx.coroutines.flow.StateFlow

interface CaregiverRelationshipRepository {
    val relationship: StateFlow<CaregiverRelationship?>

    fun saveRelationship(
        seniorId: Long,
        relationship: CaregiverRelationship,
    )
}
