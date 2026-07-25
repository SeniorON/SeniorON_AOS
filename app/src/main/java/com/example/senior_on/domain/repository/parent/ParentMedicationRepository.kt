package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentMedication

interface ParentMedicationRepository {
    suspend fun getTodayMedication(): ParentMedication?

    suspend fun hasPendingReminder(): Boolean

    suspend fun markAsTaken(medicationId: String): ParentMedication
}
