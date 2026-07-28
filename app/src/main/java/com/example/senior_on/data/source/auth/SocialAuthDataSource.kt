package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse

interface SocialAuthDataSource {
    suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse

    suspend fun handleKakaoCallback(code: String): String
}
