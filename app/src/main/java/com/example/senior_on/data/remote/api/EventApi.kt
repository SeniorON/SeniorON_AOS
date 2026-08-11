package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import retrofit2.http.*

interface EventApi {
    @POST("api/event/sos") suspend fun createSos(@Body request: SosEventRequest): ApiResponse<SosEventResponse>
    @POST("api/event/risk-link") suspend fun createRiskLink(@Body request: RiskLinkRequest): ApiResponse<RiskLinkResponse>
    @POST("api/event/outing-return") suspend fun createOutingReturn(@Body request: OutingReturnRequest): ApiResponse<OutingReturnResponse>
    @POST("api/event/inactivity") suspend fun createInactivity(@Body request: InactivityRequest): ApiResponse<InactivityResponse>
    @GET("api/event/{eventId}") suspend fun getDetail(@Path("eventId") eventId: Long): ApiResponse<EventDetailResponse>
}
