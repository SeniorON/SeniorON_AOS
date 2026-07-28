package com.example.senior_on.data.remote.dto

data class DeviceStatusUpdateRequest(
    val deviceIdentifier: String, val deviceName: String, val batteryLevel: Int
)
