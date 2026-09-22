package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface HospitalApi {
    @GET("api/hospitals/seniors/{seniorId}") suspend fun getMonthly(
        @Path("seniorId") seniorId: Long, @Query("year") year: Int, @Query("month") month: Int
    ): ApiResponse<List<HospitalListResponse>>
    @GET("api/hospitals/seniors/{seniorId}/daily") suspend fun getDaily(
        @Path("seniorId") seniorId: Long, @Query("date") date: String
    ): ApiResponse<List<HospitalDetailResponse>>
    @GET("api/hospitals/seniors/{seniorId}/upcoming")
    suspend fun getUpcoming(
        @Path("seniorId") seniorId: Long,
    ): ApiResponse<List<HospitalUpcomingResponse>>
    @POST("api/hospitals/seniors/{seniorId}") suspend fun create(
        @Path("seniorId") seniorId: Long, @Body request: HospitalCreateRequest
    ): ApiResponse<HospitalCreateResponse>
    @PUT("api/hospitals/seniors/{seniorId}/{hospitalId}") suspend fun update(
        @Path("seniorId") seniorId: Long, @Path("hospitalId") hospitalId: Long,
        @Body request: HospitalUpdateRequest
    ): ApiResponse<Unit>
    @DELETE("api/hospitals/seniors/{seniorId}/{hospitalId}") suspend fun delete(
        @Path("seniorId") seniorId: Long, @Path("hospitalId") hospitalId: Long
    ): ApiResponse<Unit>
}
