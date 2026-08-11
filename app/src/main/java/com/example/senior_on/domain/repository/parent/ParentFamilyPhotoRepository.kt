package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ParentFamilyPhotoCollection

interface ParentFamilyPhotoRepository {
    suspend fun getFamilyPhotos(
        familyCode: String
    ): List<ParentFamilyPhotoCollection>
}
