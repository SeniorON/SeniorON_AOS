package com.example.senior_on.data.remote.dto

data class SosEventRequest(val latitude: Double, val longitude: Double, val deviceBattery: Int?)
data class SosEventResponse(
    val id: Long?, val latitude: Double?, val longitude: Double?,
    val address: String?, val deviceBattery: Int?,
    val receiverCount: Int?, val notifiedCount: Int?,
)
data class RiskLinkRequest(val linkUrl: String, val deviceBattery: Int?)
data class RiskLinkResponse(
    val id: Long?, val linkUrl: String?, val riskLevel: String?, val detectedAt: String?
)
data class OutingReturnRequest(
    val phase: String, val latitude: Double, val longitude: Double, val deviceBattery: Int
)
data class OutingReturnResponse(
    val id: Long?, val phase: String?, val occurredAt: String?,
    val latitude: Double?, val longitude: Double?, val address: String?,
    val deviceBattery: Int?
)
data class InactivityRequest(
    val latitude: Double, val longitude: Double, val deviceBattery: Int,
    val lastSeenAt: String
)
data class InactivityResponse(
    val address: String?, val latitude: Double?, val longitude: Double?, val lastSeenAt: String?
)
data class EventDetailResponse(
    val eventId: Long?, val eventType: String?, val message: String?,
    val senderName: String?, val occurredAt: String?, val address: String?,
    val latitude: Double?, val longitude: Double?, val deviceBattery: Int?,
    val phase: String?, val lastSeenAt: String?, val linkUrl: String?,
    val isDangerous: Boolean?
)
