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
    val role: UserRole?,
    val accessToken: String,
    val refreshToken: String?
)

data class TokenRefreshResponse(
    val accessToken: String?,
    val refreshToken: String?
)

data class UpdateRoleResponse(
    val usersId: Long,
    val name: String,
    val role: UserRole
)

data class CheckLoginIdResponse(
    val available: Boolean
)

data class OnboardingStatusResponse(
    val hasFamily: Boolean,
    val managerType: ManagerType?,
    val seniorId: Long?,
    val seniorProfileCompleted: Boolean,
    val relation: OnboardingRelation?,
    val onboardingCompleted: Boolean,
)

enum class ManagerType {
    @SerializedName("PRIMARY")
    PRIMARY,

    @SerializedName("SUB")
    SUB,

    @SerializedName("NONE")
    NONE,
}

enum class OnboardingRelation {
    @SerializedName("MOTHER")
    MOTHER,

    @SerializedName("FATHER")
    FATHER,

    @SerializedName("GRANDPARENT")
    GRANDPARENT,

    @SerializedName("OTHER")
    OTHER,
}

enum class UserRole {
    @SerializedName("PARENT")
    PARENT,

    @SerializedName("CHILD")
    CHILD
}
