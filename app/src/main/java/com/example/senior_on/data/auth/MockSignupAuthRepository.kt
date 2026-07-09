package com.example.senior_on.data.auth

import kotlinx.coroutines.delay

class MockSignupAuthRepository : SignupAuthRepository {
    override suspend fun isUserIdAvailable(userId: String): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return userId.isNotBlank() && userId.lowercase() !in DUPLICATED_USER_IDS
    }

    override suspend fun requestEmailVerification(email: String): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return email.isNotBlank()
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String
    ): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return email.isNotBlank() && code == VALID_VERIFICATION_CODE
    }

    private companion object {
        const val VALID_VERIFICATION_CODE = "111111"
        const val MOCK_NETWORK_DELAY_MILLIS = 300L
        val DUPLICATED_USER_IDS = setOf("senioron", "user01", "test1234")
    }
}
