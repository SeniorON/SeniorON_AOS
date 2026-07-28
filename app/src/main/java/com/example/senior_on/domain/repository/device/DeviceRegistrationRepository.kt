package com.example.senior_on.domain.repository.device

import com.example.senior_on.domain.model.device.DeviceRegistration

interface DeviceRegistrationRepository {
    suspend fun getDeviceRegistration(): DeviceRegistration
}
