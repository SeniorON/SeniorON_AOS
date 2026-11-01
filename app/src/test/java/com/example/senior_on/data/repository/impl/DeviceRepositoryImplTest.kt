package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.DeviceLocationResponse
import com.example.senior_on.data.remote.dto.DeviceLocationUpdateRequest
import com.example.senior_on.data.remote.dto.FcmTokenUpdateRequest
import com.example.senior_on.data.remote.dto.HomeLocationResponse
import com.example.senior_on.data.source.device.DeviceDataSource
import com.example.senior_on.data.source.device.DeviceIdentifierDataSource
import com.example.senior_on.data.source.device.LocalDeviceStatusDataSource
import com.example.senior_on.data.source.device.LocalDeviceStatusSnapshot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceRepositoryImplTest {
    @Test fun patchReadsCurrentSnapshotOnEveryAttemptAndOmitsOtherFields() = runBlocking {
        val remote = RecordingDeviceDataSource()
        val local = MutableStatusSource()
        val repository = DeviceRepositoryImpl(remote, FixedDeviceIdentifierDataSource("test"), local)
        repository.patchStatus(true, false, false) { true }
        local.snapshot = local.snapshot.copy(batteryLevel = 30, charging = true)
        repository.patchStatus(true, false, false) { true }
        assertEquals(listOf(50, 30), remote.patches.map { it.batteryLevel })
        assertTrue(remote.patches.all { it.charging == null && it.networkConnected == null })
        repository.patchStatus(false, false, true) { true }
        assertEquals(com.example.senior_on.data.remote.dto.DeviceStatusPatchRequest("test", 30, true, true), remote.patches.last())
    }

    @Test fun offlineOrInvalidSessionDoesNotSendPatch() = runBlocking {
        val remote = RecordingDeviceDataSource()
        val local = MutableStatusSource()
        val repository = DeviceRepositoryImpl(remote, FixedDeviceIdentifierDataSource("test"), local)
        repository.patchStatus(true, true, true) { false }
        local.snapshot = local.snapshot.copy(networkConnected = false)
        repository.patchStatus(true, true, true) { true }
        assertTrue(remote.patches.isEmpty())
    }

    @Test fun patchWaitsForPutAndRechecksSessionAfterWaiting() = runBlocking {
        val entered = kotlinx.coroutines.CompletableDeferred<Unit>()
        val release = kotlinx.coroutines.CompletableDeferred<Unit>()
        val remote = RecordingDeviceDataSource()
        remote.beforePut = { entered.complete(Unit); release.await() }
        val repository = DeviceRepositoryImpl(remote, FixedDeviceIdentifierDataSource("test"), MutableStatusSource())
        var validSession = true
        val put = kotlinx.coroutines.CoroutineScope(coroutineContext).launch { repository.updateStatus() }
        entered.await()
        val patch = kotlinx.coroutines.CoroutineScope(coroutineContext).launch {
            repository.patchStatus(true, false, false) { validSession }
        }
        kotlinx.coroutines.yield()
        assertTrue(remote.patches.isEmpty())
        validSession = false
        release.complete(Unit)
        put.join()
        patch.join()
        assertTrue(remote.patches.isEmpty())
    }

    @Test
    fun updateStatusSendsCompleteLocalDeviceStatus() = runBlocking {
        val remoteSource = RecordingDeviceDataSource()
        val repository = DeviceRepositoryImpl(
            source = remoteSource,
            identifierSource = FixedDeviceIdentifierDataSource("device-123"),
            localStatusSource = FixedLocalDeviceStatusDataSource(
                deviceName = "Senior Phone",
                batteryLevel = 72,
                charging = true,
                deviceStatusSharingEnabled = true,
                networkConnected = true,
                defaultHomeEnabled = true,
                locationPermissionGranted = true,
                gpsEnabled = false,
                notificationPermissionGranted = true,
                appExecutionMaintained = false,
            ),
        )

        assertTrue(repository.updateStatus())

        assertEquals(
            DeviceStatusUpdateRequest(
                deviceIdentifier = "device-123",
                deviceName = "Senior Phone",
                batteryLevel = 72,
                charging = true,
                deviceStatusSharingEnabled = true,
                networkConnected = true,
                defaultHomeEnabled = true,
                locationPermissionGranted = true,
                gpsEnabled = false,
                notificationPermissionGranted = true,
                appExecutionMaintained = false,
            ),
            remoteSource.requests.single(),
        )
    }
}

private class RecordingDeviceDataSource : DeviceDataSource {
    val requests = mutableListOf<DeviceStatusUpdateRequest>()
    val patches = mutableListOf<com.example.senior_on.data.remote.dto.DeviceStatusPatchRequest>()
    var beforePut: suspend () -> Unit = {}

    override suspend fun patchStatus(request: com.example.senior_on.data.remote.dto.DeviceStatusPatchRequest): Boolean {
        patches += request
        return true
    }

    override suspend fun updateStatus(request: DeviceStatusUpdateRequest): Boolean {
        beforePut()
        requests += request
        return true
    }

    override suspend fun disconnect(seniorId: Long) = Unit
    override suspend fun updateFcmToken(request: FcmTokenUpdateRequest) = Unit
    override suspend fun getLatestLocation(seniorId: Long): DeviceLocationResponse =
        error("Not used")
    override suspend fun updateLocation(request: DeviceLocationUpdateRequest) = Unit
    override suspend fun getHomeLocation(): HomeLocationResponse = error("Not used")
}

private class MutableStatusSource : LocalDeviceStatusDataSource {
    var snapshot = LocalDeviceStatusSnapshot("test", 50, false, true, true, true, true, true, true, true)
    override fun getDeviceName() = snapshot.deviceName
    override fun getBatteryLevel() = snapshot.batteryLevel
    override fun getStatusSnapshot() = snapshot
}

private class FixedDeviceIdentifierDataSource(
    private val identifier: String,
) : DeviceIdentifierDataSource {
    override fun getOrCreateIdentifier(): String = identifier
}

private class FixedLocalDeviceStatusDataSource(
    private val deviceName: String,
    private val batteryLevel: Int,
    private val charging: Boolean,
    private val deviceStatusSharingEnabled: Boolean,
    private val networkConnected: Boolean,
    private val defaultHomeEnabled: Boolean,
    private val locationPermissionGranted: Boolean,
    private val gpsEnabled: Boolean,
    private val notificationPermissionGranted: Boolean,
    private val appExecutionMaintained: Boolean,
) : LocalDeviceStatusDataSource {
    override fun getDeviceName(): String = deviceName
    override fun getBatteryLevel(): Int = batteryLevel
    override fun getStatusSnapshot() = LocalDeviceStatusSnapshot(
        deviceName = deviceName,
        batteryLevel = batteryLevel,
        charging = charging,
        deviceStatusSharingEnabled = deviceStatusSharingEnabled,
        networkConnected = networkConnected,
        defaultHomeEnabled = defaultHomeEnabled,
        locationPermissionGranted = locationPermissionGranted,
        gpsEnabled = gpsEnabled,
        notificationPermissionGranted = notificationPermissionGranted,
        appExecutionMaintained = appExecutionMaintained,
    )
}
