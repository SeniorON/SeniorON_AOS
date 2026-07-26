package com.example.senior_on.data.repository.mock.parent

import com.example.senior_on.data.repository.mock.fixtures.MockFamilyPhotoFixtures
import com.example.senior_on.data.repository.mock.fixtures.MockFamilyFixtures
import com.example.senior_on.data.repository.mock.family.MockFamilyPhotoStore
import com.example.senior_on.domain.model.parent.ParentFamilyPhoto
import com.example.senior_on.domain.model.parent.ParentFamilyPhotoCollection
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import kotlinx.coroutines.delay

class MockParentFamilyPhotoRepository(
    private val photoStore: MockFamilyPhotoStore = MockFamilyPhotoStore(
        MockFamilyPhotoFixtures.initialPhotos()
    ),
) : ParentFamilyPhotoRepository {
    override suspend fun getFamilyPhotos(
        familyCode: String
    ): List<ParentFamilyPhotoCollection> {
        require(familyCode.isNotBlank()) { "Family code is required" }
        delay(350)

        return photoStore.photos.value
            .groupBy { photo -> photo.authorMemberId }
            .map { (memberId, photos) ->
                val memberName = MockFamilyFixtures.memberNameFor(memberId)
                    ?: "가족"
                ParentFamilyPhotoCollection(
                    memberId = memberId,
                    memberName = memberName,
                    photos = photos.map { photo ->
                        ParentFamilyPhoto(
                            id = photo.id,
                            memberId = memberId,
                            memberName = memberName,
                            uploadedAt = photo.createdAt,
                            imageSource = photo.imageSource,
                            message = photo.message,
                        )
                    },
                )
            }
    }
}
