package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.*

data class SeniorPermissionSettings(
    val seniorId: Long,
    val locationEnabled: Boolean,
    val inactivityDetectionEnabled: Boolean,
)

data class SeniorPermissionUpdate(
    val locationEnabled: Boolean? = null,
    val inactivityDetectionEnabled: Boolean? = null,
)

interface ParentSettingsApi {
    @GET("api/seniors/{seniorId}/permission-settings")
    suspend fun getPermissions(@Path("seniorId") seniorId: Long): ApiResponse<SeniorPermissionSettings>

    @PATCH("api/seniors/me/permission-settings")
    suspend fun updatePermissions(@Body request: SeniorPermissionUpdate): ApiResponse<SeniorPermissionSettings>

    @DELETE("api/devices/connection/me")
    suspend fun disconnect(): Response<Unit>
}
