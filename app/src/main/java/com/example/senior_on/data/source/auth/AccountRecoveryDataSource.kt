package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.dto.FindLoginIdRequest
import com.example.senior_on.data.remote.dto.FindLoginIdResponse
import com.example.senior_on.data.remote.dto.ResetPasswordRequest
import com.example.senior_on.data.remote.dto.ResetPasswordResponse
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeResponse
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeResponse

interface AccountRecoveryDataSource {
    suspend fun sendPasswordResetVerificationCode(
        request: SendPasswordResetVerificationCodeRequest
    ): SendPasswordResetVerificationCodeResponse

    suspend fun verifyPasswordResetVerificationCode(
        request: VerifyPasswordResetVerificationCodeRequest
    ): VerifyPasswordResetVerificationCodeResponse

    suspend fun findLoginId(request: FindLoginIdRequest): FindLoginIdResponse

    suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse
}
