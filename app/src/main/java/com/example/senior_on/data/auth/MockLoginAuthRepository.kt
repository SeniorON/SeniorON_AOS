package com.example.senior_on.data.auth

import com.example.senior_on.ui.app.AppUserMode

object MockLoginAuthRepository {
    fun accountModeFor(userId: String): AppUserMode? {
        return MockAuthFixtures.loginAccounts
            .firstOrNull { it.userId.equals(userId.trim(), ignoreCase = true) }
            ?.role
    }

    fun login(
        userId: String,
        password: String
    ): AppUserMode? {
        return MockAuthFixtures.loginAccounts
            .firstOrNull { account ->
                account.userId.equals(userId.trim(), ignoreCase = true) &&
                    account.password == password
            }
            ?.role
    }
}
