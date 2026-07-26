package com.example.senior_on.domain.model.auth

data class AppUserProfile(
    val userId: String,
    val name: String,
    val email: String,
    val mode: AppUserMode,
)
