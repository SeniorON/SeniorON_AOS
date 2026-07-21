package com.example.senior_on.data.auth

data class FindPasswordAccount(
    val email: String,
    val maskedEmail: String
)

object MockFindPasswordRepository {
    const val VALID_VERIFICATION_CODE = MockAuthFixtures.FIND_PASSWORD_VALID_VERIFICATION_CODE

    fun findAccount(name: String, userId: String): FindPasswordAccount? {
        val trimmedName = name.trim()
        val trimmedUserId = userId.trim()

        if (trimmedName.isBlank() || trimmedUserId.isBlank()) return null

        MockAuthFixtures.loginAccounts
            .firstOrNull {
                it.name == trimmedName && it.userId.equals(trimmedUserId, ignoreCase = true)
            }
            ?.let {
                return FindPasswordAccount(
                    email = it.email,
                    maskedEmail = maskEmail(it.email)
                )
            }

        return when {
            trimmedName == "홍길동" && trimmedUserId.equals("User_Id", ignoreCase = true) ->
                FindPasswordAccount(
                    email = "example@gmail.com",
                    maskedEmail = "ex*******@gmail.com"
                )
            trimmedName == "홍길동" ->
                FindPasswordAccount(
                    email = "example@gmail.com",
                    maskedEmail = "ex*******@gmail.com"
                )
            trimmedUserId.isNotBlank() ->
                FindPasswordAccount(
                    email = "user@senioron.com",
                    maskedEmail = maskEmail("user@senioron.com")
                )
            else -> null
        }
    }

    fun verifyCode(code: String): Boolean {
        return code == VALID_VERIFICATION_CODE
    }

    fun isValidPassword(password: String): Boolean {
        val hasLetter = password.any { it in 'a'..'z' || it in 'A'..'Z' }
        val hasDigit = password.any { it.isDigit() }
        return password.length >= 8 && hasLetter && hasDigit
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        if (atIndex <= 1) return email

        val localPart = email.substring(0, atIndex)
        val domain = email.substring(atIndex)
        val visiblePrefix = localPart.take(2)
        val maskedLength = (localPart.length - visiblePrefix.length).coerceAtLeast(3)
        return "$visiblePrefix${"*".repeat(maskedLength)}$domain"
    }
}
