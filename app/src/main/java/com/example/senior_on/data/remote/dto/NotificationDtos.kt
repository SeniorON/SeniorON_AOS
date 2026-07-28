package com.example.senior_on.data.remote.dto

data class NotificationSettingRequest(val enabled: Boolean)
data class NotificationSettingResponse(val enabled: Boolean?, val type: String?)
data class InactivitySettingRequest(val thresholdHours: Int)
data class InactivitySettingResponse(
    val usersId: Long?, val thresholdHours: Int?, val isEnabled: Boolean?
)
data class NotificationItem(
    val notificationId: Long?, val eventId: Long?, val title: String?,
    val summary: String?, val occurredAt: String?, val read: Boolean?
)
data class NotificationListResponse(
    val totalCount: Int?, val items: List<NotificationItem>?, val nextCursor: Long?
)
data class NotificationHomeResponse(
    val type: String?, val enabled: Boolean?, val hasAlert: Boolean?,
    val occurredAt: String?, val dateTimeLabel: String?, val summary: String?,
    val senderId: Long?, val senderName: String?, val deviceBattery: Int?,
    val address: String?, val linkUrl: String?, val phase: String?,
    val emptyMessage: String?
)
data class NotificationHomeListResponse(
    val enabledCount: Int?, val items: List<NotificationHomeResponse>?
)
data class ParentDeviceStatusResponse(val online: Boolean?)
