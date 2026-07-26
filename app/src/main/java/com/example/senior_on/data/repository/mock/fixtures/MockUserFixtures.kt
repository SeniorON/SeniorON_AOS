package com.example.senior_on.data.repository.mock.fixtures

import com.example.senior_on.domain.model.auth.AppUserMode
import com.example.senior_on.domain.model.auth.AppUserProfile

object MockUserFixtures {
    const val PRIMARY_CAREGIVER_USER_ID = "child"
    const val ASSISTANT_CAREGIVER_USER_ID = "child01"
    const val SECONDARY_CAREGIVER_USER_ID = "child02"
    const val SENIOR_USER_ID = "senior"
    const val SECONDARY_SENIOR_USER_ID = "senior01"

    val primaryCaregiver = AppUserProfile(
        userId = PRIMARY_CAREGIVER_USER_ID,
        name = "김민지",
        email = "child@senioron.com",
        mode = AppUserMode.Child,
    )

    val assistantCaregiver = AppUserProfile(
        userId = ASSISTANT_CAREGIVER_USER_ID,
        name = "김민니",
        email = "child01@naver.com",
        mode = AppUserMode.Child,
    )

    val secondaryCaregiver = AppUserProfile(
        userId = SECONDARY_CAREGIVER_USER_ID,
        name = "김지은",
        email = "child02@senioron.com",
        mode = AppUserMode.Child,
    )

    val senior = AppUserProfile(
        userId = SENIOR_USER_ID,
        name = "김순자",
        email = "senior@senioron.com",
        mode = AppUserMode.Senior,
    )

    val secondarySenior = AppUserProfile(
        userId = SECONDARY_SENIOR_USER_ID,
        name = "부모일번",
        email = "senior01@naver.com",
        mode = AppUserMode.Senior,
    )

    val loginProfiles = listOf(
        primaryCaregiver,
        assistantCaregiver,
        senior,
        secondarySenior,
    )

    val familyCaregiverProfiles = listOf(
        primaryCaregiver,
        assistantCaregiver,
        secondaryCaregiver,
    )

    fun profileFor(userId: String): AppUserProfile? {
        val normalizedUserId = userId.trim().lowercase()
        return (loginProfiles + secondaryCaregiver)
            .firstOrNull { profile ->
                profile.userId == normalizedUserId
            }
    }
}
