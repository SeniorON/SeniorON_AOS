package com.example.senior_on.data.auth

import com.example.senior_on.ui.app.AppUserMode

data class MockLoginAccount(
    val userId: String,
    val password: String,
    val role: AppUserMode,
    val email: String,
    val name: String
)

object MockAuthFixtures {
    const val SIGNUP_VALID_VERIFICATION_CODE = "111111"
    const val FIND_PASSWORD_VALID_VERIFICATION_CODE = "123456"
    const val VALID_FAMILY_SHARE_CODE = "43TS6GTE"
    const val DISPLAY_FAMILY_SHARE_CODE = "43TS-6GTE"

    val loginAccounts = listOf(
        MockLoginAccount(
            userId = "child",
            password = "child1234",
            role = AppUserMode.Child,
            email = "child@senioron.com",
            name = "자녀테스트"
        ),
        MockLoginAccount(
            userId = "child01",
            password = "senioron1",
            role = AppUserMode.Child,
            email = "child01@naver.com",
            name = "자녀일번"
        ),
        MockLoginAccount(
            userId = "senior",
            password = "senior1234",
            role = AppUserMode.Senior,
            email = "senior@senioron.com",
            name = "부모테스트"
        ),
        MockLoginAccount(
            userId = "senior01",
            password = "parent1234",
            role = AppUserMode.Senior,
            email = "senior01@naver.com",
            name = "부모일번"
        )
    )

    val duplicatedUserIds = setOf(
        "child",
        "child01",
        "senior",
        "senior01",
        "senioron",
        "user01",
        "test1234"
    )

    val registeredEmails = loginAccounts.map { it.email.lowercase() }.toSet() +
        setOf("already@senioron.com", "used@naver.com")

    val validSignupEmails = setOf(
        "newchild@senioron.com",
        "tester@gmail.com",
        "family@naver.com"
    )
}
