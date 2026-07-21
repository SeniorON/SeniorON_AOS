package com.example.senior_on.data.repository

import com.example.senior_on.data.model.PreparedFamilyPhoto
import com.example.senior_on.domain.model.FamilyOverview
import com.example.senior_on.domain.model.SharedFamilyPhoto
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun observeFamilyOverview(): Flow<FamilyOverview>

    suspend fun getFamilyOverview(): FamilyOverview

    suspend fun refreshFamilyOverview()

    suspend fun getSharedPhoto(photoId: String): SharedFamilyPhoto?

    suspend fun changePrimaryMember(memberId: String)

    suspend fun deleteMember(memberId: String)

    suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        message: String,
    ): SharedFamilyPhoto

    suspend fun deletePhoto(photoId: String)
}
