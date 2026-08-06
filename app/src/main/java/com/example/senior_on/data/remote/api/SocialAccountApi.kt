package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse
import com.example.senior_on.data.remote.dto.GoogleLoginRequest
import com.example.senior_on.data.remote.dto.GoogleLoginResponse
import com.example.senior_on.data.remote.dto.SocialSignupRequest
import com.example.senior_on.data.remote.dto.SocialSignupResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SocialAccountApi {
    @POST("api/social-accounts/login/kakao")
    suspend fun loginWithKakao(
        @Body request: KakaoLoginRequest
    ): ApiResponse<KakaoLoginResponse>

    @POST("api/social-accounts/login/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): ApiResponse<GoogleLoginResponse>

    @POST("api/social-accounts/signup")
    suspend fun signup(
        @Body request: SocialSignupRequest
    ): ApiResponse<SocialSignupResponse>

    @GET("api/social-accounts/login/kakao/callback")
    suspend fun kakaoCallback(
        @Query("code") code: String
    ): String
}
