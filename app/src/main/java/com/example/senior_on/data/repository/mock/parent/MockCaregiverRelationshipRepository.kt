package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockCaregiverRelationshipRepository(
    initialRelationshipLabel: String? = null,
) : CaregiverRelationshipRepository {
    private val _relationshipLabel = MutableStateFlow(initialRelationshipLabel)
    override val relationshipLabel: StateFlow<String?> =
        _relationshipLabel.asStateFlow()

    override fun saveRelationshipLabel(relationshipLabel: String) {
        _relationshipLabel.value = relationshipLabel.trim()
    }
}
