package com.example.senior_on.data.auth

interface SignupAuthRepository {
    suspend fun isUserIdAvailable(userId: String): Boolean

    suspend fun requestEmailVerification(email: String): Boolean

    suspend fun verifyEmailCode(
        email: String,
        code: String
    ): Boolean
}
