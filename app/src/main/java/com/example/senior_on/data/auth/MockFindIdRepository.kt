package com.example.senior_on.data.auth

data class FindIdAccount(
    val userId: String,
    val joinDate: String
)

object MockFindIdRepository {
    fun findAccount(name: String, email: String): FindIdAccount? {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()

        if (trimmedName.isBlank() || trimmedEmail.isBlank()) return null

        MockAuthFixtures.loginAccounts
            .firstOrNull {
                it.name == trimmedName && it.email.equals(trimmedEmail, ignoreCase = true)
            }
            ?.let {
                return FindIdAccount(userId = it.userId, joinDate = "2026.01.05")
            }

        return when {
            trimmedName == "홍길동" && trimmedEmail == "sdflsielfek@naver.com" ->
                FindIdAccount(userId = "User_Id", joinDate = "2026.01.05")
            trimmedName == "홍길동" ->
                FindIdAccount(userId = "User_Id", joinDate = "2026.01.05")
            trimmedEmail.endsWith("@senioron.com") || trimmedEmail.endsWith("@naver.com") ->
                FindIdAccount(
                    userId = trimmedEmail.substringBefore("@"),
                    joinDate = "2026.01.05"
                )
            else -> null
        }
    }
}
