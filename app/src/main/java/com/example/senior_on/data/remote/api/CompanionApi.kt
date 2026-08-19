package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.CompanionConversationEndResponse
import com.example.senior_on.data.remote.dto.CompanionConversationStartResponse
import com.example.senior_on.data.remote.dto.CompanionVoiceTurnResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CompanionApi {
    @POST("api/companion/conversations")
    suspend fun startConversation(): ApiResponse<CompanionConversationStartResponse>

    @Multipart
    @POST("api/companion/conversations/{conversationId}/voice-turn")
    suspend fun sendVoiceTurn(
        @Path("conversationId") conversationId: Long,
        @Query("requestId") requestId: String,
        @Part audio: MultipartBody.Part,
    ): ApiResponse<CompanionVoiceTurnResponse>

    @POST("api/companion/conversations/{conversationId}/end")
    suspend fun endConversation(
        @Path("conversationId") conversationId: Long,
    ): ApiResponse<CompanionConversationEndResponse>
}
