package com.example.senior_on.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SignupResponse(
    val usersId: Long,
    val name: String,
    val loginId: String
)

data class SendSignupEmailVerificationCodeResponse(
    val sent: Boolean,
    val verificationId: Long
)

data class VerifySignupEmailVerificationCodeResponse(
    val verified: Boolean
)

data class LoginResponse(
    val usersId: Long,
    val name: String,
    val loginId: String,
    val accessToken: String
)

data class UpdateRoleResponse(
    val usersId: Long,
    val name: String,
    val role: UserRole
)

data class CheckLoginIdResponse(
    val available: Boolean
)

enum class UserRole {
    @SerializedName("PARENT")
    PARENT,

    @SerializedName("CHILD")
    CHILD
}
