package com.example.senior_on.domain.model.auth

data class SignupCredentials(
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

data class SignupResult(
    val usersId: Long,
    val name: String,
    val loginId: String
)

data class LoginCredentials(
    val loginId: String,
    val password: String,
    val fcmToken: String,
    val deviceIdentifier: String
)

data class LoginResult(
    val usersId: Long,
    val name: String,
    val loginId: String,
    val accessToken: String,
    val mode: AppUserMode? = null
)

data class RoleUpdateResult(
    val usersId: Long,
    val name: String,
    val mode: AppUserMode
)
