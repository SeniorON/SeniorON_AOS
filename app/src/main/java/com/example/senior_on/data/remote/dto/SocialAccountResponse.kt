package com.example.senior_on.data.remote.dto

data class KakaoLoginResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val usersId: Long?,
    val name: String,
    val role: UserRole?,
    val providerId: String?,
    val newUser: Boolean
)

data class GoogleLoginResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val usersId: Long?,
    val name: String,
    val role: UserRole?,
    val newUser: Boolean,
)

data class SocialSignupResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val usersId: Long?,
    val name: String,
    val role: UserRole?,
    val newUser: Boolean,
)
