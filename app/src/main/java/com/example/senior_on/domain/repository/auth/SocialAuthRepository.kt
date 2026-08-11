package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.SocialLoginResult
import com.example.senior_on.domain.model.auth.SocialSignupCredentials

interface SocialAuthRepository {
    suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String,
        deviceIdentifier: String,
    ): SocialLoginResult

    suspend fun loginWithGoogle(
        firebaseIdToken: String,
        fcmToken: String,
        deviceIdentifier: String,
    ): SocialLoginResult

    suspend fun signup(credentials: SocialSignupCredentials): SocialLoginResult

    suspend fun handleKakaoCallback(code: String): String
}
