package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.FindLoginIdRequest
import com.example.senior_on.data.remote.dto.FindLoginIdResponse
import com.example.senior_on.data.remote.dto.ResetPasswordRequest
import com.example.senior_on.data.remote.dto.ResetPasswordResponse
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeResponse
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AccountRecoveryApi {
    @POST("api/users/account-recovery/password/verification-code")
    suspend fun sendPasswordResetVerificationCode(
        @Body request: SendPasswordResetVerificationCodeRequest
    ): ApiResponse<SendPasswordResetVerificationCodeResponse>

    @POST("api/users/account-recovery/password/verification-code/verify")
    suspend fun verifyPasswordResetVerificationCode(
        @Body request: VerifyPasswordResetVerificationCodeRequest
    ): ApiResponse<VerifyPasswordResetVerificationCodeResponse>

    @POST("api/users/account-recovery/login-id")
    suspend fun findLoginId(
        @Body request: FindLoginIdRequest
    ): ApiResponse<FindLoginIdResponse>

    @PATCH("api/users/account-recovery/password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ApiResponse<ResetPasswordResponse>
}
