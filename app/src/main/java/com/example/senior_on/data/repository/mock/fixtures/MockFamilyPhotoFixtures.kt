package com.example.senior_on.data.repository.mock.fixtures

import com.example.senior_on.R
import com.example.senior_on.data.repository.mock.family.MockFamilyPhotoRecord
import com.example.senior_on.domain.model.family.FamilyImageSource
import java.time.Instant

object MockFamilyPhotoFixtures {
    fun initialPhotos(
        now: Instant = Instant.now(),
    ): List<MockFamilyPhotoRecord> {
        return listOf(
            photo(
                id = "minji-1",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(45 * 60L),
                drawableResId = R.drawable.img_mock_family_primary,
                message = "오늘도 행복한 하루 보내고 맛있는 거 많이 먹고 쉬세요",
            ),
            photo(
                id = "minji-2",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(4 * 60 * 60L),
                drawableResId = R.drawable.img_family_share_picture,
                message = "나 보드게임 카페야",
            ),
            photo(
                id = "minji-3",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(12 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_assistant,
            ),
            photo(
                id = "minji-4",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(2 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_family_invitation,
            ),
            photo(
                id = "minji-5",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(4 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_primary,
            ),
            photo(
                id = "minji-6",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(7 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_assistant,
            ),
            photo(
                id = "minji-7",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(9 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_family_share_picture,
                message = "주말에 다녀온 곳이에요",
            ),
            photo(
                id = "minji-8",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(12 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_family_invitation,
            ),
            photo(
                id = "minji-9",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(15 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_primary,
                message = "예쁜 사진이 있어서 보내요",
            ),
            photo(
                id = "minji-10",
                authorMemberId = MockFamilyFixtures.PRIMARY_MEMBER_ID,
                uploadedAt = now.minusSeconds(20 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_assistant,
            ),
            photo(
                id = "jieun-1",
                authorMemberId = MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID,
                uploadedAt = now.minusSeconds(2 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_assistant,
                message = "전시회에 다녀왔어요",
            ),
            photo(
                id = "jieun-2",
                authorMemberId = MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID,
                uploadedAt = now.minusSeconds(30 * 60 * 60L),
                drawableResId = R.drawable.img_family_share_picture,
            ),
            photo(
                id = "jieun-3",
                authorMemberId = MockFamilyFixtures.SECONDARY_ASSISTANT_MEMBER_ID,
                uploadedAt = now.minusSeconds(3 * 24 * 60 * 60L),
                drawableResId = R.drawable.img_mock_family_primary,
            ),
        )
    }

    private fun photo(
        id: String,
        authorMemberId: String,
        uploadedAt: Instant,
        drawableResId: Int,
        message: String = "",
    ): MockFamilyPhotoRecord {
        return MockFamilyPhotoRecord(
            id = id,
            authorMemberId = authorMemberId,
            createdAt = uploadedAt,
            imageSource = FamilyImageSource.Local(
                drawableResId = drawableResId,
            ),
            message = message,
        )
    }
}
