package com.example.senior_on.data.model

import com.example.senior_on.R
import com.example.senior_on.data.auth.MockAuthFixtures
import com.example.senior_on.domain.model.FamilyImageSource
import com.example.senior_on.domain.model.FamilyMember
import com.example.senior_on.domain.model.FamilyMemberRole
import com.example.senior_on.domain.model.FamilyOverview
import com.example.senior_on.domain.model.SharedFamilyPhoto
import java.time.Instant

object MockFamilyFixtures {
    private val sharedPhotoCreatedAt = Instant.now().minusSeconds(4 * 60L)

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
}
