package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationNavigationEvent(
    val type: String?,
    val notificationId: Long?,
    val eventId: Long?,
    val medicationLogId: Long?,
    val hospitalId: Long?,
    val plannedDate: String?,
    val scheduleDate: String?,
    val linkUrl: String?,
    val title: String?,
)

val NotificationNavigationEvent.isMedicationNotification: Boolean
    get() = type == MedicationReminderEventStore.MedicationReminderType ||
        type == MedicationCheckedEventStore.MedicationCheckedType

val NotificationNavigationEvent.isHospitalNotification: Boolean
    get() = type == NotificationNavigationEventStore.HospitalReminderType

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
        val hospitalId = data
            .valueOf(HospitalIdKey, HospitalIdSnakeKey)
            ?.toLongOrNull()

        val isEventNotification = type in EventNotificationTypes ||
            type in MedicationNotificationTypes ||
            type == HospitalReminderType || notificationId != null ||
            eventId != null || medicationLogId != null || hospitalId != null
        if (!openNotificationTab && !isEventNotification) {
            return
        }

        _pendingEvent.value = NotificationNavigationEvent(
            type = type,
            notificationId = notificationId,
            eventId = eventId,
            medicationLogId = medicationLogId,
            hospitalId = hospitalId,
            plannedDate = data.valueOf(PlannedDateKey, PlannedDateSnakeKey),
            scheduleDate = data.valueOf(ScheduleDateKey, ScheduleDateSnakeKey),
            linkUrl = data.valueOf(LinkUrlKey, LinkUrlSnakeKey, UrlKey),
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
    const val HospitalIdKey = "hospitalId"
    const val HospitalIdSnakeKey = "hospital_id"
    const val PlannedDateKey = "plannedDate"
    const val PlannedDateSnakeKey = "planned_date"
    const val ScheduleDateKey = "scheduleDate"
    const val ScheduleDateSnakeKey = "schedule_date"
    const val LinkUrlKey = "linkUrl"
    const val LinkUrlSnakeKey = "link_url"
    const val UrlKey = "url"
    const val TitleKey = "title"
    const val HospitalReminderType = "HOSPITAL_REMINDER"

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
