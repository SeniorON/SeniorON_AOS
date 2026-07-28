package com.example.senior_on.domain.model.auth

data class KakaoLoginResult(
    val accessToken: String,
    val usersId: Long,
    val name: String,
    val mode: AppUserMode?,
    val providerId: String,
    val isNewUser: Boolean
)
