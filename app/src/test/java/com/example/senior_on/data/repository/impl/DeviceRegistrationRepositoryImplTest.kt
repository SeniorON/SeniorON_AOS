package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.device.DeviceIdentifierDataSource
import com.example.senior_on.data.source.device.FcmTokenDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceRegistrationRepositoryImplTest {
    @Test
    fun `FCM token and installation identifier are combined`() = runBlocking {
        val repository = DeviceRegistrationRepositoryImpl(
            fcmTokenDataSource = object : FcmTokenDataSource {
                override suspend fun getToken() = "fcm-token"
            },
            deviceIdentifierDataSource = object : DeviceIdentifierDataSource {
                override fun getOrCreateIdentifier() = "installation-id"
            }
        )

        val registration = repository.getDeviceRegistration()

        assertEquals("fcm-token", registration.fcmToken)
        assertEquals("installation-id", registration.deviceIdentifier)
    }
}
