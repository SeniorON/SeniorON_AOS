package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.auth.SocialAuthDataSource
import com.example.senior_on.data.remote.dto.GoogleLoginRequest
import com.example.senior_on.data.remote.dto.KakaoLoginRequest
import com.example.senior_on.data.remote.dto.SocialSignupRequest
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.SocialLoginResult
import com.example.senior_on.domain.model.auth.SocialProvider
import com.example.senior_on.domain.model.auth.SocialSignupCredentials
import com.example.senior_on.domain.repository.auth.SocialAuthRepository

class SocialAuthRepositoryImpl(
    private val dataSource: SocialAuthDataSource
) : SocialAuthRepository {
    override suspend fun loginWithKakao(
        kakaoAccessToken: String,
        fcmToken: String,
        deviceIdentifier: String,
    ): SocialLoginResult {
        val response = dataSource.loginWithKakao(
            KakaoLoginRequest(
                kakaoAccessToken = kakaoAccessToken.trim(),
                fcmToken = fcmToken,
                deviceIdentifier = deviceIdentifier,
            ),
        )
        return SocialLoginResult(
            provider = SocialProvider.Kakao,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            usersId = response.usersId,
            name = response.name,
            mode = response.role?.toAppUserMode(),
            providerId = response.providerId,
            isNewUser = response.newUser,
        )
    }

    override suspend fun loginWithGoogle(
        firebaseIdToken: String,
        fcmToken: String,
        deviceIdentifier: String,
    ): SocialLoginResult {
        val response = dataSource.loginWithGoogle(
            GoogleLoginRequest(
                firebaseIdToken = firebaseIdToken.trim(),
                fcmToken = fcmToken,
                deviceIdentifier = deviceIdentifier,
            ),
        )
        return SocialLoginResult(
            provider = SocialProvider.Google,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            usersId = response.usersId,
            name = response.name,
            mode = response.role?.toAppUserMode(),
            providerId = null,
            isNewUser = response.newUser,
        )
    }

    override suspend fun signup(
        credentials: SocialSignupCredentials,
    ): SocialLoginResult {
        val response = dataSource.signup(
            SocialSignupRequest(
                provider = credentials.provider.toApiValue(),
                socialToken = credentials.socialToken.trim(),
                name = credentials.name.trim(),
                birth = credentials.birth,
                role = credentials.mode.toRoleApiValue(),
                serviceTermsAgreed = credentials.serviceTermsAgreed,
                privacyPolicyAgreed = credentials.privacyPolicyAgreed,
                ageOver14Agreed = credentials.ageOver14Agreed,
                marketingAgreed = credentials.marketingAgreed,
                fcmToken = credentials.fcmToken,
                deviceIdentifier = credentials.deviceIdentifier,
            )
        )
        return SocialLoginResult(
            provider = credentials.provider,
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            usersId = response.usersId,
            name = response.name,
            mode = response.role?.toAppUserMode(),
            providerId = null,
            isNewUser = response.newUser,
        )
    }

    override suspend fun handleKakaoCallback(code: String): String {
        return dataSource.handleKakaoCallback(code.trim())
    }

    private fun UserRole.toAppUserMode() = when (this) {
        UserRole.CHILD -> AppUserMode.Child
        UserRole.PARENT -> AppUserMode.Senior
    }

    private fun SocialProvider.toApiValue() = when (this) {
        SocialProvider.Kakao -> "KAKAO"
        SocialProvider.Google -> "GOOGLE"
    }

    private fun AppUserMode.toRoleApiValue() = when (this) {
        AppUserMode.Child -> "CHILD"
        AppUserMode.Senior -> "PARENT"
    }
}
