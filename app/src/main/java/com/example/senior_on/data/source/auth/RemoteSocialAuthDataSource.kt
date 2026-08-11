package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.api.SocialAccountApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse
import com.example.senior_on.data.remote.dto.GoogleLoginRequest
import com.example.senior_on.data.remote.dto.GoogleLoginResponse
import com.example.senior_on.data.remote.dto.SocialSignupRequest
import com.example.senior_on.data.remote.dto.SocialSignupResponse
import com.example.senior_on.data.source.remoteRequest

class RemoteSocialAuthDataSource(
    private val socialAccountApi: SocialAccountApi
) : SocialAuthDataSource {
    override suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse {
        return remoteRequest {
            socialAccountApi.loginWithKakao(request).requireData()
        }
    }

    override suspend fun loginWithGoogle(request: GoogleLoginRequest): GoogleLoginResponse {
        return remoteRequest {
            socialAccountApi.loginWithGoogle(request).requireData()
        }
    }

    override suspend fun signup(request: SocialSignupRequest): SocialSignupResponse {
        return remoteRequest {
            socialAccountApi.signup(request).requireData()
        }
    }

    override suspend fun handleKakaoCallback(code: String): String {
        return remoteRequest {
            socialAccountApi.kakaoCallback(code.trim())
        }
    }

    private fun <T> ApiResponse<T>.requireData(): T {
        return requireNotNull(data) { message }
    }
}
