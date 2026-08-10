package com.example.senior_on.domain.model.server

import java.time.LocalDate
import java.time.LocalTime

data class ServerButton(
    val id: Long, val optionId: Long? = null, val order: Int,
    val name: String, val icon: String?, val actionType: String?,
    val actionValue: String?, val packageName: String? = null,
)
data class HomeSnapshot(
    val userName: String, val fontSize: String, val connected: Boolean,
    val battery: Int?, val buttons: List<ServerButton>,
    val seniorAddress: String? = null,
    val seniorId: Long? = null,
    val seniorName: String? = null,
    val seniorPhoneNumber: String? = null,
)
data class ServerMusicCard(
    val enabled: Boolean,
    val icon: String?,
    val musicApp: String?,
    val appName: String?,
    val actionType: String?,
    val actionValue: String?,
    val packageName: String?,
)
data class ServerTodaySchedule(
    val title: String?,
    val description: String?,
    val count: Int,
    val displayType: String?,
    val scheduleId: Long?,
    val scheduledTime: String?,
)
data class SeniorHomeSnapshot(
    val buttons: List<ServerButton>,
    val fontSize: String,
    val musicCard: ServerMusicCard?,
    val todaySchedule: ServerTodaySchedule?,
)
data class TodayHospitalSchedule(
    val id: Long,
    val hospitalName: String,
    val department: String,
    val date: LocalDate,
    val time: LocalTime,
    val reminderType: String?,
    val registeredBy: String?,
)
data class WeatherInfo(val temperature: Int, val status: String, val text: String, val observedAt: String?)
data class DeviceInfo(
    val name: String, val connected: Boolean, val status: String,
    val batteryLevel: Int?, val networkConnected: Boolean,
    val lastConnectedAt: String?, val lastLocationUpdatedAt: String?
)
data class DeviceLocation(
    val latitude: Double,
    val longitude: Double,
    val lastLocationUpdatedAt: String?,
)
data class SeniorHomeLocation(
    val latitude: Double,
    val longitude: Double,
)
data class FamilyCodeInfo(val familyId: Long?, val code: String, val memberCount: Long? = null)
data class ServerFamilyMember(
    val id: Long, val name: String, val role: String, val managerType: String,
    val canBecomePrimary: Boolean, val isMe: Boolean, val profileImageUrl: String?
)
data class ServerFamilyPhoto(
    val id: Long, val imageUrl: String, val uploaderId: Long,
    val uploaderName: String, val description: String, val createdAt: String,
    val canDelete: Boolean, val isNew: Boolean
)
data class ServerFamilyPhotoCursor(
    val createdAt: String,
    val photoId: Long,
)
data class ServerFamilyPhotoPage(
    val photos: List<ServerFamilyPhoto>,
    val totalCount: Long,
    val nextCursor: ServerFamilyPhotoCursor?,
    val hasNext: Boolean,
)
data class ServerFamilyHome(
    val members: List<ServerFamilyMember>, val recentPhotos: List<ServerFamilyPhoto>
)
data class HospitalAppointment(
    val id: Long,
    val hospitalName: String,
    val department: String,
    val date: String,
    val time: String,
    val reminderType: String,
)

data class HospitalUpcomingGroup(
    val date: String,
    val appointments: List<HospitalAppointment>,
)
data class MedicationInfo(
    val id: Long?,
    val groupId: String,
    val name: String,
    val ingredient: String?,
    val times: List<String>,
    val days: List<String>,
    val startDate: String? = null,
    val repeatType: String = "DAILY",
    val repeatInterval: Int = 1,
    val repeatEndType: String = "ONGOING",
    val durationWeeks: Int? = null,
    val endDate: String? = null,
    val medicationIds: List<Long> = emptyList(),
)
data class MedicationSchedule(
    val logId: Long,
    val name: String,
    val plannedTime: String,
    val taken: Boolean,
    val takenAt: String? = null,
    val takenTime: String? = null,
    val ingredient: String? = null,
    val plannedDate: String? = null,
    val status: String? = null,
)
data class MedicationMonthlySchedule(
    val year: Int,
    val month: Int,
    val scheduledDates: Set<LocalDate>,
)
data class AppNotification(
    val id: Long, val eventId: Long?, val title: String,
    val summary: String, val occurredAt: String, val read: Boolean
)
data class NotificationPage(
    val totalCount: Long, val items: List<AppNotification>, val nextCursor: Long?
)
data class NotificationHome(
    val enabledCount: Long,
    val items: List<NotificationHomeItem>,
)
data class NotificationHomeItem(
    val type: String,
    val enabled: Boolean,
    val hasAlert: Boolean,
    val occurredAt: String?,
    val dateTimeLabel: String?,
    val summary: String?,
    val senderId: Long?,
    val senderName: String?,
    val deviceBattery: Int?,
    val address: String?,
    val linkUrl: String?,
    val phase: String?,
    val emptyMessage: String?,
    val notificationId: Long? = null,
    val eventId: Long? = null,
)
data class NotificationSetting(val type: String, val enabled: Boolean)
data class InactivitySetting(val userId: Long, val thresholdHours: Int, val enabled: Boolean)
data class SafetyEvent(
    val id: Long?, val type: String, val occurredAt: String?,
    val address: String?, val latitude: Double?, val longitude: Double?,
    val deviceBattery: Int?, val linkUrl: String? = null,
    val dangerous: Boolean? = null, val phase: String? = null,
    val message: String? = null,
    val senderName: String? = null,
    val lastSeenAt: String? = null,
    val receiverCount: Int? = null,
    val notifiedCount: Int? = null,
)
data class UserAccountSettings(val name: String, val profileImageUrl: String?)
data class SeniorProfileUpdate(
    val seniorId: Long, val name: String, val relation: String,
    val customRelation: String?, val birth: String, val phoneNumber: String,
    val address: String?, val detailAddress: String?
)
