package com.example.senior_on.data.repository.mock.family

import com.example.senior_on.R
import com.example.senior_on.data.repository.mock.auth.MockAuthFixtures
import com.example.senior_on.domain.model.family.FamilyImageSource
import com.example.senior_on.domain.model.family.FamilyJoinResult
import com.example.senior_on.domain.model.family.FamilyMember
import com.example.senior_on.domain.model.family.FamilyMemberRole
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.SharedFamilyPhoto
import java.time.Instant

object MockFamilyFixtures {
    const val FAMILY_ID = 1L
    const val PRIMARY_CAREGIVER_USER_ID = "child"
    const val ASSISTANT_CAREGIVER_USER_ID = "child01"

    private val sharedPhotoCreatedAt = Instant.now().minusSeconds(4 * 60L)

    val assistantJoinResult = FamilyJoinResult(
        familyId = FAMILY_ID,
        familyCode = MockAuthFixtures.VALID_FAMILY_SHARE_CODE,
        memberRole = FamilyMemberRole.Assistant,
    )

    val primaryCaregiverOverview = FamilyOverview(
        members = listOf(
            FamilyMember(
                id = "family-member-primary",
                name = "김민지",
                role = FamilyMemberRole.Primary,
                isCurrentUser = true,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            ),
            FamilyMember(
                id = "family-member-assistant-1",
                name = "김민니",
                role = FamilyMemberRole.Assistant,
                isCurrentUser = false,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_assistant
                )
            ),
            FamilyMember(
                id = "family-member-assistant-2",
                name = "ㅇㅇㅇ",
                role = FamilyMemberRole.Assistant,
                isCurrentUser = false,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_assistant
                )
            )
        ),
        sharedPhotos = listOf(
            SharedFamilyPhoto(
                id = "shared-photo-1",
                authorName = "김민지",
                createdAt = sharedPhotoCreatedAt,
                isOwnedByCurrentUser = true,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            ),
            SharedFamilyPhoto(
                id = "shared-photo-2",
                authorName = "김민지",
                createdAt = sharedPhotoCreatedAt,
                isOwnedByCurrentUser = true,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            ),
            SharedFamilyPhoto(
                id = "shared-photo-3",
                authorName = "김민니",
                createdAt = sharedPhotoCreatedAt,
                isOwnedByCurrentUser = true,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            ),
            SharedFamilyPhoto(
                id = "shared-photo-4",
                authorName = "김민지",
                createdAt = sharedPhotoCreatedAt,
                isOwnedByCurrentUser = true,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            ),
            SharedFamilyPhoto(
                id = "shared-photo-5",
                authorName = "PM 연두",
                createdAt = sharedPhotoCreatedAt,
                isOwnedByCurrentUser = false,
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                )
            )
        ),
        invitationCode = MockAuthFixtures.DISPLAY_FAMILY_SHARE_CODE
    )

    val assistantCaregiverOverview = primaryCaregiverOverview.copy(
        members = primaryCaregiverOverview.members.map { member ->
            member.copy(
                isCurrentUser = member.id == "family-member-assistant-1"
            )
        },
        sharedPhotos = primaryCaregiverOverview.sharedPhotos.map { photo ->
            photo.copy(isOwnedByCurrentUser = photo.authorName == "김민니")
        }
    )

    fun overviewForUserId(userId: String): FamilyOverview {
        return when (userId.trim().lowercase()) {
            ASSISTANT_CAREGIVER_USER_ID -> assistantCaregiverOverview
            else -> primaryCaregiverOverview
        }
    }
}
