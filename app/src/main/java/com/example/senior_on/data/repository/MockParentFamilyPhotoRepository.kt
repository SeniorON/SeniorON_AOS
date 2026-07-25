package com.example.senior_on.data.repository

import com.example.senior_on.R
import com.example.senior_on.domain.model.FamilyImageSource
import com.example.senior_on.domain.model.ParentFamilyPhoto
import com.example.senior_on.domain.model.ParentFamilyPhotoCollection
import java.time.Instant
import kotlinx.coroutines.delay

class MockParentFamilyPhotoRepository : ParentFamilyPhotoRepository {
    override suspend fun getFamilyPhotos(
        familyCode: String
    ): List<ParentFamilyPhotoCollection> {
        require(familyCode.isNotBlank()) { "Family code is required" }
        delay(350)

        val now = Instant.now()
        return listOf(
            ParentFamilyPhotoCollection(
                memberId = "family-minji",
                memberName = "김민지",
                photos = listOf(
                    ParentFamilyPhoto(
                        id = "minji-1",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(45 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_primary
                        ),
                        message = "오늘도 행복한 하루 보내고 맛있는 거 많이 먹고 쉬세요"
                    ),
                    ParentFamilyPhoto(
                        id = "minji-2",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(4 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_family_share_picture
                        ),
                        message = "나 보드게임 카페야"
                    ),
                    ParentFamilyPhoto(
                        id = "minji-3",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(12 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_assistant
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "minji-4",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(2 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_family_invitation
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "minji-5",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(4 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_primary
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "minji-6",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(7 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_assistant
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "minji-7",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(9 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_family_share_picture
                        ),
                        message = "주말에 다녀온 곳이에요"
                    ),
                    ParentFamilyPhoto(
                        id = "minji-8",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(12 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_family_invitation
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "minji-9",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(15 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_primary
                        ),
                        message = "예쁜 사진이 있어서 보내요"
                    ),
                    ParentFamilyPhoto(
                        id = "minji-10",
                        memberId = "family-minji",
                        memberName = "김민지",
                        uploadedAt = now.minusSeconds(20 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_assistant
                        )
                    )
                )
            ),
            ParentFamilyPhotoCollection(
                memberId = "family-jieun",
                memberName = "김지은",
                photos = listOf(
                    ParentFamilyPhoto(
                        id = "jieun-1",
                        memberId = "family-jieun",
                        memberName = "김지은",
                        uploadedAt = now.minusSeconds(2 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_assistant
                        ),
                        message = "전시회에 다녀왔어요"
                    ),
                    ParentFamilyPhoto(
                        id = "jieun-2",
                        memberId = "family-jieun",
                        memberName = "김지은",
                        uploadedAt = now.minusSeconds(30 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_family_share_picture
                        )
                    ),
                    ParentFamilyPhoto(
                        id = "jieun-3",
                        memberId = "family-jieun",
                        memberName = "김지은",
                        uploadedAt = now.minusSeconds(3 * 24 * 60 * 60L),
                        imageSource = FamilyImageSource.Local(
                            R.drawable.img_mock_family_primary
                        )
                    )
                )
            )
        )
    }
}
