package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface MedicationApi {
    @GET("api/medications/seniors/{seniorId}")
    suspend fun getMedications(@Path("seniorId") seniorId: Long): ApiResponse<List<MedicationReadResponse>>
    @POST("api/medications/seniors/{seniorId}") suspend fun create(
        @Path("seniorId") seniorId: Long, @Body request: MedicationCreateRequest
    ): ApiResponse<MedicationCreateResponse>
    @PUT("api/medications/seniors/{seniorId}") suspend fun update(
        @Path("seniorId") seniorId: Long, @Body request: MedicationUpdateRequest
    ): ApiResponse<String>
    @DELETE("api/medications/seniors/{seniorId}/groups/{medicationGroupId}") suspend fun delete(
        @Path("seniorId") seniorId: Long, @Path("medicationGroupId") medicationGroupId: String
    ): ApiResponse<String>
    @GET("api/v1/medications/schedules") suspend fun getMySchedules(
        @Query("date") date: String
    ): ApiResponse<List<MedicationScheduleResponse>>
    @GET("api/v1/medications/seniors/{seniorId}/schedules") suspend fun getParentSchedules(
        @Path("seniorId") seniorId: Long, @Query("date") date: String
    ): ApiResponse<List<MedicationScheduleResponse>>
    @GET("api/v1/medications/seniors/{seniorId}/schedules/monthly")
    suspend fun getParentMonthlySchedules(
        @Path("seniorId") seniorId: Long,
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): ApiResponse<MedicationMonthlyScheduleResponse>
    @PATCH("api/v1/medication-logs/check")
    suspend fun checkNearest(): ApiResponse<MedicationCheckResponse>

    @PATCH("api/v1/medication-logs/{medicationLogId}/check")
    suspend fun check(
        @Path("medicationLogId") medicationLogId: Long,
    ): ApiResponse<MedicationCheckResponse>
}
