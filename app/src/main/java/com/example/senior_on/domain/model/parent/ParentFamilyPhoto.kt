package com.example.senior_on.domain.model.parent

import com.example.senior_on.domain.model.family.FamilyImageSource
import java.time.Instant

data class ParentFamilyPhoto(
    val id: String,
    val memberId: String,
    val memberName: String,
    val uploadedAt: Instant,
    val imageSource: FamilyImageSource,
    val message: String = ""
)

data class ParentFamilyPhotoCollection(
    val memberId: String,
    val memberName: String,
    val photos: List<ParentFamilyPhoto>
) {
    val latestPhoto: ParentFamilyPhoto?
        get() = photos.maxByOrNull(ParentFamilyPhoto::uploadedAt)
}
