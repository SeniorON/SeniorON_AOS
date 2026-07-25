package com.example.senior_on.data.repository

import com.example.senior_on.domain.model.ParentFamilyPhotoCollection

interface ParentFamilyPhotoRepository {
    suspend fun getFamilyPhotos(
        familyCode: String
    ): List<ParentFamilyPhotoCollection>
}
