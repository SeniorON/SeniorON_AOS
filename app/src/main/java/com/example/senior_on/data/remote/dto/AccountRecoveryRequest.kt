package com.example.senior_on.data.remote.dto

data class SendPasswordResetVerificationCodeRequest(
    val name: String,
    val loginId: String
)

data class VerifyPasswordResetVerificationCodeRequest(
    val verificationId: Long,
    val verificationCode: String
)

data class FindLoginIdRequest(
    val name: String,
    val email: String
)

data class ResetPasswordRequest(
    val verificationId: Long,
    val newPassword: String,
    val newPasswordCheck: String
)
