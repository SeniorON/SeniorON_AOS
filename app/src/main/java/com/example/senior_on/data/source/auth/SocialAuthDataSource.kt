package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse
import com.example.senior_on.data.remote.dto.GoogleLoginRequest
import com.example.senior_on.data.remote.dto.GoogleLoginResponse
import com.example.senior_on.data.remote.dto.SocialSignupRequest
import com.example.senior_on.data.remote.dto.SocialSignupResponse

interface SocialAuthDataSource {
    suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse

    suspend fun loginWithGoogle(request: GoogleLoginRequest): GoogleLoginResponse

    suspend fun signup(request: SocialSignupRequest): SocialSignupResponse

    suspend fun handleKakaoCallback(code: String): String
}
