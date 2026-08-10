package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.api.UserApi
import com.example.senior_on.data.remote.dto.CheckLoginIdResponse
import com.example.senior_on.data.remote.dto.LoginRequest
import com.example.senior_on.data.remote.dto.LoginResponse
import com.example.senior_on.data.remote.dto.OnboardingStatusResponse
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendSignupEmailVerificationCodeResponse
import com.example.senior_on.data.remote.dto.SignupRequest
import com.example.senior_on.data.remote.dto.SignupResponse
import com.example.senior_on.data.remote.dto.UpdateRoleRequest
import com.example.senior_on.data.remote.dto.UpdateRoleResponse
import com.example.senior_on.data.remote.dto.UserLogoutRequest
import com.example.senior_on.data.remote.dto.UserWithdrawalRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeResponse
import com.example.senior_on.data.source.remoteRequest

class RemoteAuthDataSource(
    private val userApi: UserApi
) : AuthDataSource {
    override suspend fun checkLoginId(loginId: String): CheckLoginIdResponse {
        return userApi.checkLoginId(loginId).requireData()
    }

    override suspend fun sendSignupEmailVerificationCode(
        request: SendSignupEmailVerificationCodeRequest
    ): SendSignupEmailVerificationCodeResponse {
        return remoteRequest {
            userApi.sendSignupEmailVerificationCode(request).requireData()
        }
    }

    override suspend fun verifySignupEmailVerificationCode(
        request: VerifySignupEmailVerificationCodeRequest
    ): VerifySignupEmailVerificationCodeResponse {
        return userApi.verifySignupEmailVerificationCode(request).requireData()
    }

    override suspend fun signup(request: SignupRequest): SignupResponse {
        return userApi.signup(request).requireData()
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return userApi.login(request).requireData()
    }

    override suspend fun getOnboardingStatus(): OnboardingStatusResponse {
        return userApi.getOnboardingStatus().requireData()
    }

    override suspend fun updateRole(
        authorization: String,
        request: UpdateRoleRequest
    ): UpdateRoleResponse {
        return userApi.updateRole(authorization, request).requireData()
    }

    override suspend fun logout(request: UserLogoutRequest) {
        remoteRequest {
            userApi.logout(request).requireData()
            Unit
        }
    }

    override suspend fun withdraw(request: UserWithdrawalRequest) {
        remoteRequest {
            userApi.withdraw(request).requireData()
            Unit
        }
    }

    private fun <T> com.example.senior_on.data.remote.dto.ApiResponse<T>.requireData(): T {
        return requireNotNull(data) { message }
    }

}
