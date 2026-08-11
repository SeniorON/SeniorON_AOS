package com.example.senior_on.data.source.device

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.DeviceLocationResponse
import com.example.senior_on.data.remote.dto.DeviceLocationUpdateRequest
import com.example.senior_on.data.remote.dto.HomeLocationResponse
import com.example.senior_on.data.remote.dto.FcmTokenUpdateRequest
import com.example.senior_on.data.source.remoteRequest
import retrofit2.HttpException
import retrofit2.Response

interface DeviceDataSource {
    suspend fun updateStatus(request: DeviceStatusUpdateRequest): Boolean
    suspend fun updateFcmToken(request: FcmTokenUpdateRequest)
    suspend fun disconnect()
    suspend fun getLatestLocation(): DeviceLocationResponse
    suspend fun updateLocation(request: DeviceLocationUpdateRequest)
    suspend fun getHomeLocation(): HomeLocationResponse
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

    override suspend fun updateFcmToken(request: FcmTokenUpdateRequest) {
        remoteRequest {
            api.updateFcmToken(request).requireSuccessful()
        }
    }

    override suspend fun getLatestLocation(): DeviceLocationResponse =
        remoteRequest { api.getLatestLocation() }

    override suspend fun updateLocation(request: DeviceLocationUpdateRequest) {
        remoteRequest {
            api.updateLocation(request).requireSuccessful()
        }
    }

    override suspend fun getHomeLocation(): HomeLocationResponse =
        remoteRequest { api.getHomeLocation() }
}

private fun Response<Unit>.requireSuccessful() {
    if (!isSuccessful) throw HttpException(this)
}

private const val DEVICE_NOT_CONNECTED_HTTP_STATUS = 404
