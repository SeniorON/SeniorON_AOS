package com.example.senior_on.data.remote.dto

data class DeviceStatusUpdateRequest(
    val deviceIdentifier: String, val deviceName: String, val batteryLevel: Int
)

data class FcmTokenUpdateRequest(
    val deviceIdentifier: String,
    val deviceToken: String,
)

data class DeviceLocationUpdateRequest(
    val deviceIdentifier: String,
    val latitude: Double,
    val longitude: Double,
)

data class DeviceLocationResponse(
    val latitude: Double?,
    val longitude: Double?,
    val lastLocationUpdatedAt: String?,
)

data class HomeLocationResponse(
    val latitude: Double?,
    val longitude: Double?,
)
