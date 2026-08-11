package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationNavigationEvent(
    val type: String?,
    val notificationId: Long?,
    val eventId: Long?,
    val medicationLogId: Long?,
    val title: String?,
)

val NotificationNavigationEvent.isMedicationNotification: Boolean
    get() = type == MedicationReminderEventStore.MedicationReminderType ||
        type == MedicationCheckedEventStore.MedicationCheckedType

object NotificationNavigationEventStore {
    private val _pendingEvent = MutableStateFlow<NotificationNavigationEvent?>(null)
    val pendingEvent = _pendingEvent.asStateFlow()

    fun publish(
        data: Map<String, String>,
        openNotificationTab: Boolean = false,
    ) {
        val type = data.valueOf(TypeKey, EventTypeKey, NotificationTypeKey)
        val notificationId = data
            .valueOf(NotificationIdKey, NotificationIdSnakeKey)
            ?.toLongOrNull()
        val eventId = data
            .valueOf(EventIdKey, EventIdSnakeKey)
            ?.toLongOrNull()
        val medicationLogId = data
            .valueOf(MedicationLogIdKey, MedicationLogIdSnakeKey)
            ?.toLongOrNull()

        val isEventNotification = type in EventNotificationTypes ||
            type in MedicationNotificationTypes ||
            notificationId != null || eventId != null || medicationLogId != null
        if (!openNotificationTab && !isEventNotification) {
            return
        }

        _pendingEvent.value = NotificationNavigationEvent(
            type = type,
            notificationId = notificationId,
            eventId = eventId,
            medicationLogId = medicationLogId,
            title = data.valueOf(TitleKey),
        )
    }

    fun consume() {
        _pendingEvent.value = null
    }

    private fun Map<String, String>.valueOf(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> this[key]?.takeIf(String::isNotBlank) }

    const val OpenNotificationAction =
        "com.example.senior_on.action.OPEN_NOTIFICATION"
    const val TypeKey = "type"
    const val EventTypeKey = "eventType"
    const val NotificationTypeKey = "notificationType"
    const val NotificationIdKey = "notificationId"
    const val NotificationIdSnakeKey = "notification_id"
    const val EventIdKey = "eventId"
    const val EventIdSnakeKey = "event_id"
    const val MedicationLogIdKey = "medicationLogId"
    const val MedicationLogIdSnakeKey = "medication_log_id"
    const val TitleKey = "title"

    private val EventNotificationTypes = setOf(
        "SOS",
        "INACTIVITY",
        "RISK_LINK",
        "OUTING_RETURN",
    )

    private val MedicationNotificationTypes = setOf(
        MedicationReminderEventStore.MedicationReminderType,
        MedicationCheckedEventStore.MedicationCheckedType,
    )
}
