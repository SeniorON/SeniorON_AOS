package com.example.senior_on.data.repository.mock.auth

import com.example.senior_on.data.repository.mock.fixtures.MockAuthFixtures
import com.example.senior_on.domain.model.auth.AppUserMode

object MockLoginAuthRepository {
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
