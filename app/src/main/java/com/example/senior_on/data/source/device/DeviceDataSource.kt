package com.example.senior_on.data.source.device

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest

interface DeviceDataSource {
    suspend fun updateStatus(request: DeviceStatusUpdateRequest)
    suspend fun disconnect()
}

class RemoteDeviceDataSource(private val api: DeviceApi) : DeviceDataSource {
    override suspend fun updateStatus(request: DeviceStatusUpdateRequest) { api.updateStatus(request) }
    override suspend fun disconnect() { api.disconnect() }
}
