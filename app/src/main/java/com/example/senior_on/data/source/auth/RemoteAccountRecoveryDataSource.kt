package com.example.senior_on.data.source.auth

import com.example.senior_on.data.remote.api.AccountRecoveryApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.FindLoginIdRequest
import com.example.senior_on.data.remote.dto.ResetPasswordRequest
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.FindLoginIdResponse
import com.example.senior_on.data.remote.dto.ResetPasswordResponse
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeResponse
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeResponse

class RemoteAccountRecoveryDataSource(
    private val accountRecoveryApi: AccountRecoveryApi
) : AccountRecoveryDataSource {
    override suspend fun sendPasswordResetVerificationCode(
        request: SendPasswordResetVerificationCodeRequest
    ): SendPasswordResetVerificationCodeResponse {
        return accountRecoveryApi.sendPasswordResetVerificationCode(request).requireData()
    }

    override suspend fun verifyPasswordResetVerificationCode(
        request: VerifyPasswordResetVerificationCodeRequest
    ): VerifyPasswordResetVerificationCodeResponse {
        return accountRecoveryApi.verifyPasswordResetVerificationCode(request).requireData()
    }

    override suspend fun findLoginId(request: FindLoginIdRequest): FindLoginIdResponse {
        return accountRecoveryApi.findLoginId(request).requireData()
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): ResetPasswordResponse {
        return accountRecoveryApi.resetPassword(request).requireData()
    }

    private fun <T> ApiResponse<T>.requireData(): T {
        return requireNotNull(data) { message }
    }
}
