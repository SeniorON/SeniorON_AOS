package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface HospitalApi {
    @GET("api/hospitals/parents/{parentUserId}") suspend fun getMonthly(
        @Path("parentUserId") parentUserId: Long, @Query("year") year: Int, @Query("month") month: Int
    ): ApiResponse<List<HospitalListResponse>>
    @GET("api/hospitals/parents/{parentUserId}/daily") suspend fun getDaily(
        @Path("parentUserId") parentUserId: Long, @Query("date") date: String
    ): ApiResponse<List<HospitalDetailResponse>>
    @GET("api/hospitals/parents/{parentUserId}/upcoming")
    suspend fun getUpcoming(
        @Path("parentUserId") parentUserId: Long,
    ): ApiResponse<List<HospitalUpcomingResponse>>
    @POST("api/hospitals/parents/{parentUserId}") suspend fun create(
        @Path("parentUserId") parentUserId: Long, @Body request: HospitalCreateRequest
    ): ApiResponse<HospitalCreateResponse>
    @PUT("api/hospitals/parents/{parentUserId}/{hospitalId}") suspend fun update(
        @Path("parentUserId") parentUserId: Long, @Path("hospitalId") hospitalId: Long,
        @Body request: HospitalUpdateRequest
    ): ApiResponse<Unit>
    @DELETE("api/hospitals/parents/{parentUserId}/{hospitalId}") suspend fun delete(
        @Path("parentUserId") parentUserId: Long, @Path("hospitalId") hospitalId: Long
    ): ApiResponse<Unit>
}
