package com.example.senior_on.data.source.mock.fixtures

import com.example.senior_on.R
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyJoinResult
import com.example.senior_on.domain.model.family.FamilyMember
import com.example.senior_on.domain.model.family.FamilyMemberRole
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.SharedFamilyPhoto

object MockFamilyFixtures {
    const val FAMILY_ID = 1L
    const val PRIMARY_MEMBER_ID = "family-member-primary"
    const val ASSISTANT_MEMBER_ID = "family-member-assistant-1"
    const val SECONDARY_ASSISTANT_MEMBER_ID = "family-member-assistant-2"

    val assistantJoinResult = FamilyJoinResult(
        familyId = FAMILY_ID,
        familyCode = MockAuthFixtures.VALID_FAMILY_SHARE_CODE,
        memberRole = FamilyMemberRole.Assistant,
    )

    private val members = listOf(
        FamilyMember(
            id = PRIMARY_MEMBER_ID,
            name = MockUserFixtures.primaryCaregiver.name,
            role = FamilyMemberRole.Primary,
            isCurrentUser = false,
            imageSource = FamilyImageSource.Local(
                drawableResId = R.drawable.img_mock_family_primary
            )
        ),
        FamilyMember(
            id = ASSISTANT_MEMBER_ID,
            name = MockUserFixtures.assistantCaregiver.name,
            role = FamilyMemberRole.Assistant,
            isCurrentUser = false,
            imageSource = FamilyImageSource.Local(
                drawableResId = R.drawable.img_mock_family_assistant
            )
        ),
        FamilyMember(
            id = SECONDARY_ASSISTANT_MEMBER_ID,
            name = MockUserFixtures.secondaryCaregiver.name,
            role = FamilyMemberRole.Assistant,
            isCurrentUser = false,
            imageSource = FamilyImageSource.Local(
                drawableResId = R.drawable.img_mock_family_assistant
            )
        )
    )

    private val sharedPhotos = MockFamilyPhotoFixtures.initialPhotos()
        .sortedByDescending { photo -> photo.createdAt }
        .map { photo ->
            SharedFamilyPhoto(
                id = photo.id,
                authorName = requireNotNull(memberNameFor(photo.authorMemberId)),
                createdAt = photo.createdAt,
                isOwnedByCurrentUser = false,
                imageSource = photo.imageSource,
                message = photo.message,
            )
        }

    val primaryCaregiverOverview: FamilyOverview
        get() = overviewForUserId(MockUserFixtures.primaryCaregiver.userId)

    val assistantCaregiverOverview: FamilyOverview
        get() = overviewForUserId(MockUserFixtures.assistantCaregiver.userId)

    fun memberNameFor(memberId: String): String? {
        return members.firstOrNull { member -> member.id == memberId }?.name
    }

    fun overviewForUserId(userId: String): FamilyOverview {
        val currentProfile = MockUserFixtures.profileFor(userId)
            ?: MockUserFixtures.primaryCaregiver

        return FamilyOverview(
            members = members.map { member ->
                member.copy(
                    isCurrentUser = when (member.id) {
                        PRIMARY_MEMBER_ID ->
                            currentProfile.userId ==
                                MockUserFixtures.primaryCaregiver.userId

                        ASSISTANT_MEMBER_ID ->
                            currentProfile.userId ==
                                MockUserFixtures.assistantCaregiver.userId

                        SECONDARY_ASSISTANT_MEMBER_ID ->
                            currentProfile.userId ==
                                MockUserFixtures.secondaryCaregiver.userId

                        else -> false
                    }
                )
            },
            sharedPhotos = sharedPhotos.map { photo ->
                photo.copy(
                    isOwnedByCurrentUser =
                        photo.authorName == currentProfile.name
                )
            },
            invitationCode = MockAuthFixtures.DISPLAY_FAMILY_SHARE_CODE,
        )
    }
}
