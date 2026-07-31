package com.example.senior_on.data.source.device

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.source.remoteRequest
import retrofit2.HttpException
import retrofit2.Response

interface DeviceDataSource {
    suspend fun updateStatus(request: DeviceStatusUpdateRequest)
    suspend fun disconnect()
}

class RemoteDeviceDataSource(private val api: DeviceApi) : DeviceDataSource {
    override suspend fun updateStatus(request: DeviceStatusUpdateRequest) {
        remoteRequest {
            api.updateStatus(request).requireSuccessful()
        }
    }

    override suspend fun disconnect() {
        remoteRequest {
            api.disconnect().requireSuccessful()
        }
    }
}

private fun Response<Unit>.requireSuccessful() {
    if (!isSuccessful) throw HttpException(this)
}
