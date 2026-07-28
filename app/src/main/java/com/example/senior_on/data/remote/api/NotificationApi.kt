package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface NotificationApi {
    @GET("api/notification") suspend fun getNotifications(
        @Query("type") type: String, @Query("cursor") cursor: Long? = null,
        @Query("size") size: Int? = null
    ): ApiResponse<NotificationListResponse>
    @PATCH("api/notification/{notificationId}/read")
    suspend fun markRead(@Path("notificationId") notificationId: Long): ApiResponse<Unit>
    @DELETE("api/notification/{notificationId}")
    suspend fun delete(@Path("notificationId") notificationId: Long): ApiResponse<Unit>
    @GET("api/notification/setting")
    suspend fun getSettings(): ApiResponse<NotificationHomeListResponse>
    @PATCH("api/notification/setting/{type}") suspend fun updateSetting(
        @Path("type") type: String, @Body request: NotificationSettingRequest
    ): ApiResponse<NotificationSettingResponse>
    @GET("api/notification/parent-device-status")
    suspend fun getParentDeviceStatus(): ApiResponse<ParentDeviceStatusResponse>
    @GET("api/inactivity-settings/{targetUserId}")
    suspend fun getInactivitySetting(@Path("targetUserId") targetUserId: Long): ApiResponse<InactivitySettingResponse>
    @PATCH("api/inactivity-settings/{targetUserId}") suspend fun updateInactivitySetting(
        @Path("targetUserId") targetUserId: Long, @Body request: InactivitySettingRequest
    ): ApiResponse<InactivitySettingResponse>
}
