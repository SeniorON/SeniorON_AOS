package com.example.senior_on.domain.repository.parent

import kotlinx.coroutines.flow.StateFlow

interface CaregiverRelationshipRepository {
    val relationshipLabel: StateFlow<String?>

    fun saveRelationshipLabel(relationshipLabel: String)
}
