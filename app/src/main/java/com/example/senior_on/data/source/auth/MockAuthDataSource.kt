package com.example.senior_on.data.source.auth

import com.example.senior_on.data.source.mock.fixtures.MockAuthFixtures
import com.example.senior_on.data.remote.dto.CheckLoginIdResponse
import com.example.senior_on.data.remote.dto.LoginRequest
import com.example.senior_on.data.remote.dto.LoginResponse
import com.example.senior_on.data.remote.dto.ManagerType
import com.example.senior_on.data.remote.dto.OnboardingStatusResponse
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeResponse
import com.example.senior_on.data.remote.dto.SignupRequest
import com.example.senior_on.data.remote.dto.SignupResponse
import com.example.senior_on.data.remote.dto.UpdateRoleRequest
import com.example.senior_on.data.remote.dto.UpdateRoleResponse
import com.example.senior_on.data.remote.dto.UserLogoutRequest
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.remote.dto.UserWithdrawalRequest
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
            loginId = request.loginId.lowercase(),
            role = request.role,
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

    override suspend fun getOnboardingStatus(): OnboardingStatusResponse {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return OnboardingStatusResponse(
            hasFamily = true,
            managerType = ManagerType.PRIMARY,
            seniorId = 1L,
            seniorProfileCompleted = true,
            relation = null,
            onboardingCompleted = true,
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

    override suspend fun logout(request: UserLogoutRequest) {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        require(request.deviceIdentifier.isNotBlank()) {
            "기기 식별자가 필요합니다."
        }
    }

    override suspend fun withdraw(request: UserWithdrawalRequest) {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        require(request.confirmation == WITHDRAWAL_CONFIRMATION) {
            "확인 문구가 올바르지 않습니다."
        }
    }

    private companion object {
        const val MOCK_NETWORK_DELAY_MILLIS = 300L
        const val MOCK_SIGNUP_USER_ID = 1L
        const val MOCK_ACCESS_TOKEN = "mock-access-token"
        const val MOCK_REFRESH_TOKEN = "mock-refresh-token"
        const val WITHDRAWAL_CONFIRMATION = "회원 탈퇴"
    }
}
