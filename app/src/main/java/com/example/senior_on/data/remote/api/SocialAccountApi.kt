package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SocialAccountApi {
    @POST("api/social-accounts/login/kakao")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): ApiResponse<KakaoLoginResponse>

    @GET("api/social-accounts/login/kakao/callback")
    suspend fun kakaoCallback(
        @Query("code") code: String
    ): String
}
