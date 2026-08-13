package com.example.senior_on.data.remote.dto

data class SendPasswordResetVerificationCodeResponse(
    val sent: Boolean,
    val verificationId: Long
)

data class VerifyPasswordResetVerificationCodeResponse(
    val verified: Boolean
)

data class FindLoginIdResponse(
    val loginId: String,
    val createdAt: String,
)

data class ResetPasswordResponse(
    val reset: Boolean
)
