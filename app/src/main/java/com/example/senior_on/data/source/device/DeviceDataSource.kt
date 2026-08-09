package com.example.senior_on.data.source.device

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.source.remoteRequest
import retrofit2.HttpException
import retrofit2.Response

interface DeviceDataSource {
    suspend fun updateStatus(request: DeviceStatusUpdateRequest): Boolean
    suspend fun disconnect()
}

class RemoteDeviceDataSource(private val api: DeviceApi) : DeviceDataSource {
    override suspend fun updateStatus(request: DeviceStatusUpdateRequest): Boolean =
        remoteRequest {
            val response = api.updateStatus(request)
            if (response.code() == DEVICE_NOT_CONNECTED_HTTP_STATUS) {
                false
            } else {
                response.requireSuccessful()
                true
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

private const val DEVICE_NOT_CONNECTED_HTTP_STATUS = 404
