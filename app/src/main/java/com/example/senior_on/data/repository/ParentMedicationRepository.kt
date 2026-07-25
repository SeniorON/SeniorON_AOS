package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentMedication

interface ParentMedicationRepository {
    suspend fun getTodayMedication(): ParentMedication?

    suspend fun hasPendingReminder(): Boolean

    suspend fun markAsTaken(medicationId: String): ParentMedication
}
