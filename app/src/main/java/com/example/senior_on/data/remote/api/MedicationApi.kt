package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface MedicationApi {
    @GET("api/medications/parents/{parentUserId}")
    suspend fun getMedications(@Path("parentUserId") parentUserId: Long): ApiResponse<List<MedicationReadResponse>>
    @POST("api/medications/parents/{parentUserId}") suspend fun create(
        @Path("parentUserId") parentUserId: Long, @Body request: MedicationCreateRequest
    ): ApiResponse<MedicationCreateResponse>
    @PUT("api/medications/parents/{parentUserId}") suspend fun update(
        @Path("parentUserId") parentUserId: Long, @Body request: MedicationUpdateRequest
    ): ApiResponse<String>
    @DELETE("api/medications/parents/{parentUserId}/groups/{medicationGroupId}") suspend fun delete(
        @Path("parentUserId") parentUserId: Long, @Path("medicationGroupId") medicationGroupId: String
    ): ApiResponse<String>
    @GET("api/v1/medications/schedules") suspend fun getMySchedules(
        @Query("date") date: String
    ): ApiResponse<List<MedicationScheduleResponse>>
    @GET("api/v1/medications/parents/{parentUserId}/schedules") suspend fun getParentSchedules(
        @Path("parentUserId") parentUserId: Long, @Query("date") date: String
    ): ApiResponse<List<MedicationScheduleResponse>>
    @PATCH("api/v1/medication-logs/{medicationLogId}/check") suspend fun check(
        @Path("medicationLogId") medicationLogId: Long
    ): ApiResponse<MedicationCheckResponse>
}
