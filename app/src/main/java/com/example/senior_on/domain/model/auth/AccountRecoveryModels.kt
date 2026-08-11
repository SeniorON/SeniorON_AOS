package com.example.senior_on.domain.model.auth

data class PasswordResetVerificationDelivery(
    val sent: Boolean,
    val verificationId: Long
)

data class FoundLoginId(
    val loginId: String
)

data class AuthSession(
    val role: AppUserMode,
    val userId: String,
)

fun isValidPassword(password: String): Boolean {
    val hasLetter = password.any { it in 'a'..'z' || it in 'A'..'Z' }
    val hasDigit = password.any(Char::isDigit)
    return password.length >= 8 && hasLetter && hasDigit
}
