package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT

interface DeviceApi {
    @PUT("api/devices/status")
    suspend fun updateStatus(@Body request: DeviceStatusUpdateRequest): Response<Unit>

    @DELETE("api/devices/connection")
    suspend fun disconnect(): Response<Unit>
}
