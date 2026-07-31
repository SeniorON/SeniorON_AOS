package com.example.senior_on.notification

import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MedicationReminderEvent(
    val medicationLogId: Long,
    val medicineName: String,
    val plannedTime: LocalTime,
)

object MedicationReminderEventStore {
    private val _pendingEvent = MutableStateFlow<MedicationReminderEvent?>(null)
    val pendingEvent = _pendingEvent.asStateFlow()

    fun publish(data: Map<String, String>) {
        if (data[NotificationTypeKey] != MedicationReminderType) return
        val event = MedicationReminderEvent(
            medicationLogId = data[MedicationLogIdKey]?.toLongOrNull() ?: 0L,
            medicineName = data[MedicineNameKey].orEmpty(),
            plannedTime = data[PlannedTimeKey].toLocalTimeOrNull() ?: LocalTime.MIDNIGHT,
        )
        _pendingEvent.value = event
    }

    fun consume() {
        _pendingEvent.value = null
    }

    private fun String?.toLocalTimeOrNull(): LocalTime? = try {
        this?.let(LocalTime::parse)
    } catch (_: DateTimeParseException) {
        null
    }

    const val NotificationTypeKey = "type"
    const val MedicationReminderType = "MEDICATION_REMINDER"
    const val MedicationLogIdKey = "medicationLogId"
    const val MedicineNameKey = "medicineName"
    const val PlannedTimeKey = "plannedTime"
}
