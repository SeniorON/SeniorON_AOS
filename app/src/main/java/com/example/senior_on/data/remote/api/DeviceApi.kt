package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.DeviceStatusUpdateRequest
import com.example.senior_on.data.remote.dto.DeviceLocationResponse
import com.example.senior_on.data.remote.dto.DeviceLocationUpdateRequest
import com.example.senior_on.data.remote.dto.HomeLocationResponse
import com.example.senior_on.data.remote.dto.FcmTokenUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface DeviceApi {
    @PUT("api/devices/status")
    suspend fun updateStatus(@Body request: DeviceStatusUpdateRequest): Response<Unit>

    @PATCH("api/devices/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenUpdateRequest): Response<Unit>

    @DELETE("api/devices/connection")
    suspend fun disconnect(): Response<Unit>

    @GET("api/devices/location")
    suspend fun getLatestLocation(): DeviceLocationResponse

    @PATCH("api/devices/location")
    suspend fun updateLocation(
        @Body request: DeviceLocationUpdateRequest,
    ): Response<Unit>

    @GET("api/devices/home-location")
    suspend fun getHomeLocation(): HomeLocationResponse
}
