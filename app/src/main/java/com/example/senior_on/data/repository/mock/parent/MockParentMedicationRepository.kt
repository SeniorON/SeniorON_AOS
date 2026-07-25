package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.domain.model.parent.ParentMedication
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.delay

enum class MockParentMedicationScenario {
    Scheduled,
    Empty
}

class MockParentMedicationRepository(
    private val scenario: MockParentMedicationScenario =
        MockParentMedicationScenario.Scheduled
) : ParentMedicationRepository {
    private var medication = ParentMedication(
        id = "blood-pressure-medicine",
        name = "혈압약",
        scheduledTime = LocalTime.of(14, 0)
    )

    override suspend fun getTodayMedication(): ParentMedication? {
        delay(220)
        return when (scenario) {
            MockParentMedicationScenario.Scheduled -> medication
            MockParentMedicationScenario.Empty -> null
        }
    }

    override suspend fun hasPendingReminder(): Boolean =
        scenario == MockParentMedicationScenario.Scheduled &&
            medication.takenAt == null

    override suspend fun markAsTaken(medicationId: String): ParentMedication {
        require(scenario == MockParentMedicationScenario.Scheduled) {
            "No medication is scheduled"
        }
        require(medication.id == medicationId) {
            "Medication not found: $medicationId"
        }
        delay(350)
        medication = medication.copy(takenAt = Instant.now())
        return medication
    }
}
