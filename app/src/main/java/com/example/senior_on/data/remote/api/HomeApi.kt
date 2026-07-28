package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface HomeApi {
    @GET("api/home") suspend fun getHome(): ApiResponse<HomeResponse>
    @GET("api/home/weather") suspend fun getWeather(
        @Query("latitude") latitude: Double, @Query("longitude") longitude: Double
    ): ApiResponse<WeatherResponse>
    @GET("api/home/senior") suspend fun getSeniorHome(): ApiResponse<SeniorHomeResponse>
    @GET("api/home/hospitals/today")
    suspend fun getTodayHospitals(): ApiResponse<List<TodayHospitalListResponse>>
    @GET("api/home/device") suspend fun getDevice(): ApiResponse<DeviceDetailResponse>
    @GET("api/home/button-options")
    suspend fun getButtonOptions(): ApiResponse<List<ButtonOptionResponse>>
    @PUT("api/home/buttons") suspend fun saveButtons(@Body request: HomeButtonSaveRequest): ApiResponse<Unit>
    @POST("api/home/buttons")
    suspend fun addButton(@Body request: HomeButtonCreateRequest): ApiResponse<HomeButtonCreateResponse>
    @PATCH("api/home/buttons") suspend fun updateButtons(@Body request: HomeButtonUpdateRequest): ApiResponse<Unit>
    @DELETE("api/home/buttons/{buttonId}") suspend fun deleteButton(@Path("buttonId") buttonId: Long): ApiResponse<Unit>
    @PATCH("api/home/font-size") suspend fun updateFontSize(@Body request: HomeFontSizeUpdateRequest): ApiResponse<Unit>
    @PATCH("api/home/senior-profile")
    suspend fun updateSeniorProfile(@Body request: SeniorProfileUpdateRequest): ApiResponse<SeniorProfileUpdateResponse>
}
