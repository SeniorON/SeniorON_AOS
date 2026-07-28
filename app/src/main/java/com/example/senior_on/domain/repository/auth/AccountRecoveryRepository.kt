package com.example.senior_on.domain.repository.auth

import com.example.senior_on.domain.model.auth.FoundLoginId
import com.example.senior_on.domain.model.auth.PasswordResetVerificationDelivery

interface AccountRecoveryRepository {
    suspend fun sendPasswordResetVerificationCode(
        name: String,
        loginId: String
    ): PasswordResetVerificationDelivery

    suspend fun verifyPasswordResetVerificationCode(
        verificationId: Long,
        verificationCode: String
    ): Boolean

    suspend fun findLoginId(
        name: String,
        email: String
    ): FoundLoginId

    suspend fun resetPassword(
        verificationId: Long,
        newPassword: String,
        newPasswordCheck: String
    ): Boolean
}
