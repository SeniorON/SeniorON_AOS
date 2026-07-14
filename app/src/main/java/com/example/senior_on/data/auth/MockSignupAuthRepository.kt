package com.example.senior_on.data.auth

import kotlinx.coroutines.delay

class MockSignupAuthRepository : SignupAuthRepository {
    override suspend fun isUserIdAvailable(userId: String): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return userId.isNotBlank() &&
            userId.trim().lowercase() !in MockAuthFixtures.duplicatedUserIds
    }

    override suspend fun requestEmailVerification(email: String): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        val normalizedEmail = email.trim().lowercase()
        return normalizedEmail.isNotBlank() &&
            normalizedEmail !in MockAuthFixtures.registeredEmails
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String
    ): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return email.isNotBlank() && code == MockAuthFixtures.SIGNUP_VALID_VERIFICATION_CODE
    }

    private companion object {
        const val MOCK_NETWORK_DELAY_MILLIS = 300L
    }
}
