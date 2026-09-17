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
    suspend fun disconnect(seniorId: Long)
    suspend fun getLatestLocation(seniorId: Long): DeviceLocationResponse
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

    override suspend fun disconnect(seniorId: Long) {
        remoteRequest {
            api.disconnect(seniorId).requireSuccessful()
        }
    }

    override suspend fun updateFcmToken(request: FcmTokenUpdateRequest) {
        remoteRequest {
            api.updateFcmToken(request).requireSuccessful()
        }
    }

    override suspend fun getLatestLocation(seniorId: Long): DeviceLocationResponse =
        remoteRequest { api.getLatestLocation(seniorId) }

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
