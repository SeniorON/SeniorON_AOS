package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.KakaoLoginResult

interface SocialAuthRepository {
    suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResult

    suspend fun handleKakaoCallback(code: String): String
}
