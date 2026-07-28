package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.auth.AccountRecoveryDataSource
import com.example.senior_on.data.remote.dto.FindLoginIdRequest
import com.example.senior_on.data.remote.dto.ResetPasswordRequest
import com.example.senior_on.data.remote.dto.SendPasswordResetVerificationCodeRequest
import com.example.senior_on.data.remote.dto.VerifyPasswordResetVerificationCodeRequest
import com.example.senior_on.domain.model.auth.FoundLoginId
import com.example.senior_on.domain.model.auth.PasswordResetVerificationDelivery
import com.example.senior_on.domain.repository.auth.AccountRecoveryRepository

class AccountRecoveryRepositoryImpl(
    private val dataSource: AccountRecoveryDataSource
) : AccountRecoveryRepository {
    override suspend fun sendPasswordResetVerificationCode(
        name: String,
        loginId: String
    ): PasswordResetVerificationDelivery {
        val response = dataSource.sendPasswordResetVerificationCode(
            SendPasswordResetVerificationCodeRequest(name.trim(), loginId.trim())
        )
        return PasswordResetVerificationDelivery(response.sent, response.verificationId)
    }

    override suspend fun verifyPasswordResetVerificationCode(
        verificationId: Long,
        verificationCode: String
    ): Boolean {
        return dataSource.verifyPasswordResetVerificationCode(
            VerifyPasswordResetVerificationCodeRequest(
                verificationId,
                verificationCode.trim()
            )
        ).verified
    }

    override suspend fun findLoginId(name: String, email: String): FoundLoginId {
        val response = dataSource.findLoginId(FindLoginIdRequest(name.trim(), email.trim()))
        return FoundLoginId(response.loginId)
    }

    override suspend fun resetPassword(
        verificationId: Long,
        newPassword: String,
        newPasswordCheck: String
    ): Boolean {
        return dataSource.resetPassword(
            ResetPasswordRequest(verificationId, newPassword, newPasswordCheck)
        ).reset
    }
}
