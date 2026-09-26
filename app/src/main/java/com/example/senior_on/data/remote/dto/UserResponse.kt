package com.example.senior_on.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SignupResponse(
    val usersId: Long,
    val name: String,
    val loginId: String,
    val role: UserRole,
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
    val onboardingCompleted: Boolean,
    val currentUserRole: UserRole?,
    val families: List<OnboardingFamilyStatusResponse>?,
)

data class OnboardingFamilyStatusResponse(
    val familyId: Long,
    val managerType: ManagerType?,
    val parentUserId: Long?,
    val relation: OnboardingRelation?,
    val seniorId: Long?,
    val seniorName: String?,
    val seniorProfileCompleted: Boolean,
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
