package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface HomeApi {
    @GET("api/home")
    suspend fun getHome(@Query("seniorId") seniorId: Long): ApiResponse<HomeResponse>

    @GET("api/home/senior") suspend fun getSeniorHome(): ApiResponse<SeniorHomeResponse>

    @GET("api/home/hospitals/today")
    suspend fun getTodayHospitals(
        @Query("seniorId") seniorId: Long,
    ): ApiResponse<List<TodayHospitalListResponse>>

    @GET("api/home/device")
    suspend fun getDevice(
        @Query("seniorId") seniorId: Long,
    ): ApiResponse<DeviceDetailResponse>

    @GET("api/home/button-options")
    suspend fun getButtonOptions(
        @Query("seniorId") seniorId: Long,
    ): ApiResponse<List<ButtonOptionResponse>>

    @PUT("api/home/buttons")
    suspend fun saveButtons(
        @Body request: HomeButtonSaveRequest,
    ): ApiResponse<Unit>

    @PATCH("api/home/font-size")
    suspend fun updateFontSize(
        @Body request: HomeFontSizeUpdateRequest,
    ): ApiResponse<Unit>

    @PATCH("api/home/senior-profile")
    suspend fun updateSeniorProfile(
        @Body request: SeniorProfileUpdateRequest,
    ): ApiResponse<SeniorProfileUpdateResponse>
}
