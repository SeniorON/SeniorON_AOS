package com.example.senior_on.data.source.auth

import com.example.senior_on.data.source.mock.fixtures.MockAuthFixtures
import com.example.senior_on.data.remote.dto.CheckLoginIdResponse
import com.example.senior_on.data.remote.dto.LoginRequest
import com.example.senior_on.data.remote.dto.LoginResponse
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeResponse
import com.example.senior_on.data.remote.dto.SignupRequest
import com.example.senior_on.data.remote.dto.SignupResponse
import com.example.senior_on.data.remote.dto.UpdateRoleRequest
import com.example.senior_on.data.remote.dto.UpdateRoleResponse
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeResponse
import com.example.senior_on.domain.model.auth.AppUserMode
import kotlinx.coroutines.delay

class MockAuthDataSource : AuthDataSource {
    override suspend fun checkLoginId(loginId: String): CheckLoginIdResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return CheckLoginIdResponse(
            available = loginId.isNotBlank() &&
                loginId.lowercase() !in MockAuthFixtures.duplicatedUserIds
        )
    }

    override suspend fun sendSignupEmailVerificationCode(
        request: SendSignupEmailVerificationCodeRequest
    ): SendSignupEmailVerificationCodeResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        val normalizedEmail = request.email.lowercase()
        return SendSignupEmailVerificationCodeResponse(
            sent = normalizedEmail.isNotBlank() &&
                normalizedEmail !in MockAuthFixtures.registeredEmails,
            verificationId = 1L
        )
    }

    override suspend fun verifySignupEmailVerificationCode(
        request: VerifySignupEmailVerificationCodeRequest
    ): VerifySignupEmailVerificationCodeResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return VerifySignupEmailVerificationCodeResponse(
            verified = request.email.isNotBlank() &&
                request.verificationCode == MockAuthFixtures.SIGNUP_VALID_VERIFICATION_CODE
        )
    }

    override suspend fun signup(request: SignupRequest): SignupResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return SignupResponse(
            usersId = MOCK_SIGNUP_USER_ID,
            name = request.name,
            loginId = request.loginId.lowercase()
        )
    }

    override suspend fun login(request: LoginRequest): LoginResponse? {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        val account = MockAuthFixtures.loginAccounts.firstOrNull { account ->
            account.userId.equals(request.loginId, ignoreCase = true) &&
                account.password == request.password
        } ?: return null

        return LoginResponse(
            usersId = account.profile.userId.hashCode().toLong(),
            name = account.name,
            loginId = account.userId,
            role = when (account.role) {
                AppUserMode.Child -> UserRole.CHILD
                AppUserMode.Senior -> UserRole.PARENT
            },
            accessToken = MOCK_ACCESS_TOKEN,
            refreshToken = MOCK_REFRESH_TOKEN,
        )
    }

    override suspend fun updateRole(
        authorization: String,
        request: UpdateRoleRequest
    ): UpdateRoleResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return UpdateRoleResponse(
            usersId = MOCK_SIGNUP_USER_ID,
            name = "",
            role = request.role
        )
    }

    private companion object {
        const val MOCK_NETWORK_DELAY_MILLIS = 300L
        const val MOCK_SIGNUP_USER_ID = 1L
        const val MOCK_ACCESS_TOKEN = "mock-access-token"
        const val MOCK_REFRESH_TOKEN = "mock-refresh-token"
    }
}
