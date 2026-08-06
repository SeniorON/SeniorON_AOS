package com.example.senior_on.data.source.auth

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
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifySignupEmailVerificationCodeResponse

interface AuthDataSource {
    suspend fun checkLoginId(loginId: String): CheckLoginIdResponse

    suspend fun sendSignupEmailVerificationCode(
        request: SendSignupEmailVerificationCodeRequest
    ): SendSignupEmailVerificationCodeResponse

    suspend fun verifySignupEmailVerificationCode(
        request: VerifySignupEmailVerificationCodeRequest
    ): VerifySignupEmailVerificationCodeResponse

    suspend fun signup(request: SignupRequest): SignupResponse

    suspend fun login(request: LoginRequest): LoginResponse?

    suspend fun getOnboardingStatus(): OnboardingStatusResponse

    suspend fun updateRole(
        authorization: String,
        request: UpdateRoleRequest
    ): UpdateRoleResponse
}
