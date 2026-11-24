package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

internal data class ConsentPayload(val medicationConsent: Boolean?)
internal data class GuardianConsentPayload(val connectionStatus: String?, val medicationConsent: Boolean?)

internal interface HealthConsentApi {
    @GET("api/users/me/medication-consent")
    suspend fun mine(): ApiResponse<ConsentPayload>

    @PATCH("api/users/me/medication-consent")
    suspend fun updateMine(@Body body: ConsentPayload): ApiResponse<ConsentPayload>

    @GET("api/seniors/{seniorId}/medication-consent")
    suspend fun forSenior(@Path("seniorId") seniorId: Long): ApiResponse<GuardianConsentPayload>
}
