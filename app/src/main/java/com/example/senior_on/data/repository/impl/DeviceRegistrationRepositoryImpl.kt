package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.device.DeviceIdentifierDataSource
import com.example.senior_on.data.source.device.FcmTokenDataSource
import com.example.senior_on.domain.model.device.DeviceRegistration
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository

class DeviceRegistrationRepositoryImpl(
    private val fcmTokenDataSource: FcmTokenDataSource,
    private val deviceIdentifierDataSource: DeviceIdentifierDataSource
) : DeviceRegistrationRepository {
    override suspend fun getDeviceRegistration(): DeviceRegistration {
        return DeviceRegistration(
            fcmToken = fcmTokenDataSource.getToken(),
            deviceIdentifier = deviceIdentifierDataSource.getOrCreateIdentifier()
        )
    }
}
