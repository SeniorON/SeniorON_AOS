package com.example.senior_on.data.remote.dto

data class SignupRequest(
    val loginId: String,
    val email: String,
    val password: String,
    val passwordCheck: String,
    val name: String,
    val birth: String,
    val agreeServiceTerms: Boolean,
    val agreePrivacyPolicy: Boolean,
    val agreeAgeOver14: Boolean,
    val agreeMarketing: Boolean
)

data class SendSignupEmailVerificationCodeRequest(
    val email: String
)

data class VerifySignupEmailVerificationCodeRequest(
    val email: String,
    val verificationCode: String
)

data class LoginRequest(
    val loginId: String,
    val password: String,
    val fcmToken: String,
    val deviceIdentifier: String
)

data class TokenRefreshRequest(
    val refreshToken: String,
    val deviceIdentifier: String?
)

data class UpdateRoleRequest(
    val role: UserRole
)
