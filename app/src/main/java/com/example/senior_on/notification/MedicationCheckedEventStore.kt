package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MedicationCheckedEvent(
    val parentUserId: Long,
    val medicationLogId: Long,
    val medicineName: String,
)

object MedicationCheckedEventStore {
    private val _pendingEvent = MutableStateFlow<MedicationCheckedEvent?>(null)
    val pendingEvent = _pendingEvent.asStateFlow()

    fun publish(data: Map<String, String>) {
        if (data[NotificationTypeKey] != MedicationCheckedType) return

        val parentUserId = data[ParentUserIdKey]?.toLongOrNull() ?: return
        val medicationLogId = data[MedicationLogIdKey]?.toLongOrNull() ?: return
        _pendingEvent.value = MedicationCheckedEvent(
            parentUserId = parentUserId,
            medicationLogId = medicationLogId,
            medicineName = data[MedicineNameKey].orEmpty(),
        )
    }

    fun consume() {
        _pendingEvent.value = null
    }

    const val NotificationTypeKey = "type"
    const val MedicationCheckedType = "MEDICATION_CHECKED"
    const val ParentUserIdKey = "parentUserId"
    const val MedicationLogIdKey = "medicationLogId"
    const val MedicineNameKey = "medicineName"
}
