package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.device.DeviceIdentifierDataSource
import com.example.senior_on.data.source.device.LocalDeviceStatusDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceRepositoryImplTest {
    @Test
    fun updateStatusSendsIdentifierNameAndBatteryFromLocalSources() = runBlocking {
        val remoteSource = RecordingDeviceDataSource()
        val repository = DeviceRepositoryImpl(
            source = remoteSource,
            identifierSource = FixedDeviceIdentifierDataSource("device-123"),
            localStatusSource = FixedLocalDeviceStatusDataSource(
                deviceName = "Senior Phone",
                batteryLevel = 72,
            ),
        )

        assertTrue(repository.updateStatus())

        assertEquals(
            DeviceStatusUpdateRequest(
                deviceIdentifier = "device-123",
                deviceName = "Senior Phone",
                batteryLevel = 72,
            ),
            remoteSource.requests.single(),
        )
    }
}

private class RecordingDeviceDataSource : DeviceDataSource {
    val requests = mutableListOf<DeviceStatusUpdateRequest>()

    override suspend fun updateStatus(request: DeviceStatusUpdateRequest): Boolean {
        requests += request
        return true
    }

    override suspend fun disconnect() = Unit
}

private class FixedDeviceIdentifierDataSource(
    private val identifier: String,
) : DeviceIdentifierDataSource {
    override fun getOrCreateIdentifier(): String = identifier
}

private class FixedLocalDeviceStatusDataSource(
    private val deviceName: String,
    private val batteryLevel: Int,
) : LocalDeviceStatusDataSource {
    override fun getDeviceName(): String = deviceName
    override fun getBatteryLevel(): Int = batteryLevel
}
