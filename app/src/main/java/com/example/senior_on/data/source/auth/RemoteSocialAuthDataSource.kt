package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.api.SocialAccountApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginResponse

class RemoteSocialAuthDataSource(
    private val socialAccountApi: SocialAccountApi
) : SocialAuthDataSource {
    override suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse {
        return socialAccountApi.loginWithKakao(request).requireData()
    }

    override suspend fun handleKakaoCallback(code: String): String {
        return socialAccountApi.kakaoCallback(code.trim())
    }

    private fun <T> ApiResponse<T>.requireData(): T {
        return requireNotNull(data) { message }
    }
}
