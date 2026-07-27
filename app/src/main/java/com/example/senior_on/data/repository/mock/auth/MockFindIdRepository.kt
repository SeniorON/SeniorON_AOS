package com.example.senior_on.data.repository.mock.auth

import com.example.senior_on.data.repository.mock.fixtures.MockAuthFixtures

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
                return FindIdAccount(
                    userId = it.userId,
                    joinDate = MockAuthFixtures.DEFAULT_JOIN_DATE,
                )
            }

        return when {
            trimmedName == "홍길동" && trimmedEmail == "sdflsielfek@naver.com" ->
                FindIdAccount(
                    userId = "User_Id",
                    joinDate = MockAuthFixtures.DEFAULT_JOIN_DATE,
                )
            trimmedName == "홍길동" ->
                FindIdAccount(
                    userId = "User_Id",
                    joinDate = MockAuthFixtures.DEFAULT_JOIN_DATE,
                )
            trimmedEmail.endsWith("@senioron.com") || trimmedEmail.endsWith("@naver.com") ->
                FindIdAccount(
                    userId = trimmedEmail.substringBefore("@"),
                    joinDate = MockAuthFixtures.DEFAULT_JOIN_DATE,
                )
            else -> null
        }
    }
}
