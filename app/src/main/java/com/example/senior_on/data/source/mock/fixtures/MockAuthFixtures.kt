package com.example.senior_on.data.source.mock.fixtures

import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AppUserProfile

data class MockLoginAccount(
    val profile: AppUserProfile,
    val password: String,
) {
    val userId: String
        get() = profile.userId

    val role: AppUserMode
        get() = profile.mode

    val email: String
        get() = profile.email

    val name: String
        get() = profile.name
}

object MockAuthFixtures {
    const val SIGNUP_VALID_VERIFICATION_CODE = "111111"
    const val FIND_PASSWORD_VALID_VERIFICATION_CODE = "123456"
    const val VALID_FAMILY_SHARE_CODE = "43TS6GTE"
    const val DISPLAY_FAMILY_SHARE_CODE = "43TS-6GTE"
    const val DEFAULT_JOIN_DATE = "2026.01.05"

    val loginAccounts = listOf(
        MockLoginAccount(
            profile = MockUserFixtures.primaryCaregiver,
            password = "child1234",
        ),
        MockLoginAccount(
            profile = MockUserFixtures.assistantCaregiver,
            password = "senioron1",
        ),
        MockLoginAccount(
            profile = MockUserFixtures.senior,
            password = "senior1234",
        ),
        MockLoginAccount(
            profile = MockUserFixtures.secondarySenior,
            password = "parent1234",
        ),
    )

    val duplicatedUserIds = loginAccounts
        .map { account -> account.userId }
        .toSet() + setOf(
            "senioron",
            "user01",
            "test1234",
        )

    val registeredEmails = loginAccounts
        .map { account -> account.email.lowercase() }
        .toSet() + setOf(
            "already@senioron.com",
            "used@naver.com",
        )

    val validSignupEmails = setOf(
        "newchild@senioron.com",
        "tester@gmail.com",
        "family@naver.com",
    )
}
