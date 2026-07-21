package com.example.senior_on.data.auth

import com.example.senior_on.ui.app.AppUserMode

object MockLoginAuthRepository {
    fun accountModeFor(userId: String): AppUserMode? {
        return when (userId.trim().lowercase()) {
            "senior", "senior01", "senioron", "user" -> AppUserMode.Senior
            "child", "child01" -> AppUserMode.Child
            else -> null
        }
    }

    fun login(
        userId: String,
        password: String
    ): AppUserMode? {
        if (password.isBlank()) return null
        return accountModeFor(userId)
    }
}
