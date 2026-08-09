package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.auth.AuthDataSource
import com.example.senior_on.data.remote.dto.LoginRequest
import com.example.senior_on.data.remote.dto.ManagerType
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SignupRequest
import com.example.senior_on.data.remote.dto.UpdateRoleRequest
import com.example.senior_on.data.remote.dto.UserLogoutRequest
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.remote.dto.UserWithdrawalRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeRequest
import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.CareManagerType
import com.example.senior_on.domain.model.auth.LoginCredentials
import com.example.senior_on.domain.model.auth.LoginResult
import com.example.senior_on.domain.model.auth.OnboardingStatus
import com.example.senior_on.domain.model.auth.RoleUpdateResult
import com.example.senior_on.domain.model.auth.SignupCredentials
import com.example.senior_on.domain.model.auth.SignupResult
import com.example.senior_on.domain.repository.auth.AuthRepository

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource
) : AuthRepository {
    override suspend fun isLoginIdAvailable(loginId: String): Boolean {
        return dataSource.checkLoginId(loginId.trim()).available
    }

    override suspend fun sendSignupEmailVerificationCode(email: String): Boolean {
        return dataSource.sendSignupEmailVerificationCode(
            SendSignupEmailVerificationCodeRequest(email.trim())
        ).sent
    }

    override suspend fun verifySignupEmailVerificationCode(
        email: String,
        verificationCode: String
    ): Boolean {
        return dataSource.verifySignupEmailVerificationCode(
            VerifySignupEmailVerificationCodeRequest(
                email = email.trim(),
                verificationCode = verificationCode.trim()
            )
        ).verified
    }

    override suspend fun signup(credentials: SignupCredentials): SignupResult {
        val response = dataSource.signup(
            SignupRequest(
                loginId = credentials.loginId.trim(),
                email = credentials.email.trim(),
                password = credentials.password,
                passwordCheck = credentials.passwordCheck,
                name = credentials.name.trim(),
                birth = credentials.birth,
                agreeServiceTerms = credentials.agreeServiceTerms,
                agreePrivacyPolicy = credentials.agreePrivacyPolicy,
                agreeAgeOver14 = credentials.agreeAgeOver14,
                agreeMarketing = credentials.agreeMarketing
            )
        )
        return SignupResult(response.usersId, response.name, response.loginId)
    }

    override suspend fun login(credentials: LoginCredentials): LoginResult? {
        val response = dataSource.login(
            LoginRequest(
                loginId = credentials.loginId.trim(),
                password = credentials.password,
                fcmToken = credentials.fcmToken,
                deviceIdentifier = credentials.deviceIdentifier
            )
        ) ?: return null
        return LoginResult(
            usersId = response.usersId,
            name = response.name,
            loginId = response.loginId,
            accessToken = response.accessToken,
            mode = response.role?.toAppUserMode(),
            refreshToken = response.refreshToken,
        )
    }

    override suspend fun getOnboardingStatus(): OnboardingStatus {
        val response = dataSource.getOnboardingStatus()
        return OnboardingStatus(
            hasFamily = response.hasFamily,
            managerType = when (response.managerType) {
                ManagerType.PRIMARY -> CareManagerType.Primary
                ManagerType.SUB -> CareManagerType.Sub
                ManagerType.NONE, null -> CareManagerType.None
            },
            seniorId = response.seniorId,
            seniorProfileCompleted = response.seniorProfileCompleted,
            relationRegistered = response.relation != null,
            onboardingCompleted = response.onboardingCompleted,
        )
    }

    override suspend fun updateRole(
        accessToken: String,
        mode: AppUserMode
    ): RoleUpdateResult {
        val response = dataSource.updateRole(
            authorization = accessToken.toBearerToken(),
            request = UpdateRoleRequest(mode.toUserRole())
        )
        return RoleUpdateResult(
            usersId = response.usersId,
            name = response.name,
            mode = response.role.toAppUserMode()
        )
    }

    override suspend fun logout(deviceIdentifier: String) {
        dataSource.logout(
            UserLogoutRequest(deviceIdentifier = deviceIdentifier.trim())
        )
    }

    override suspend fun withdraw(confirmation: String) {
        dataSource.withdraw(
            UserWithdrawalRequest(confirmation = confirmation)
        )
    }

    private fun AppUserMode.toUserRole() = when (this) {
        AppUserMode.Child -> UserRole.CHILD
        AppUserMode.Senior -> UserRole.PARENT
    }

    private fun UserRole.toAppUserMode() = when (this) {
        UserRole.CHILD -> AppUserMode.Child
        UserRole.PARENT -> AppUserMode.Senior
    }

    private fun String.toBearerToken(): String =
        if (startsWith(BEARER_PREFIX, ignoreCase = true)) this else "$BEARER_PREFIX$this"

    private companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}
