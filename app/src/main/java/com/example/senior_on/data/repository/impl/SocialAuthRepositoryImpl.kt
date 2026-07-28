package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.auth.SocialAuthDataSource
import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.KakaoLoginResult
import com.example.senior_on.domain.repository.auth.SocialAuthRepository

class SocialAuthRepositoryImpl(
    private val dataSource: SocialAuthDataSource
) : SocialAuthRepository {
    override suspend fun loginWithKakao(kakaoAccessToken: String): KakaoLoginResult {
        val response = dataSource.loginWithKakao(KakaoLoginRequest(kakaoAccessToken.trim()))
        AccessTokenStore.save(response.accessToken)
        return KakaoLoginResult(
            accessToken = response.accessToken,
            usersId = response.usersId,
            name = response.name,
            mode = response.role?.toAppUserMode(),
            providerId = response.providerId,
            isNewUser = response.newUser
        )
    }

    override suspend fun handleKakaoCallback(code: String): String {
        return dataSource.handleKakaoCallback(code.trim())
    }

    private fun UserRole.toAppUserMode() = when (this) {
        UserRole.CHILD -> AppUserMode.Child
        UserRole.PARENT -> AppUserMode.Senior
    }
}
