package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SeniorApi {
    @POST("api/seniors")
    suspend fun createSenior(
        @Header("Authorization") authorization: String,
        @Body request: CreateSeniorRequest
    ): ApiResponse<CreateSeniorResponse>

    @PATCH("api/seniors/{seniorId}/relation")
    suspend fun updateSeniorRelation(
        @Header("Authorization") authorization: String,
        @Path("seniorId") seniorId: Long,
        @Body request: UpdateSeniorRelationRequest
    ): ApiResponse<UpdateSeniorRelationResponse>
}
