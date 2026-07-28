package com.example.senior_on.data.source.family

import com.example.senior_on.domain.model.family.FamilyJoinResult
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.family.SharedFamilyPhoto
import kotlinx.coroutines.flow.Flow

interface FamilyDataSource {
    suspend fun joinFamily(familyCode: String): FamilyJoinResult
    fun observeFamilyOverview(): Flow<FamilyOverview>
    suspend fun getFamilyOverview(): FamilyOverview
    suspend fun refreshFamilyOverview()
    suspend fun getSharedPhoto(photoId: String): SharedFamilyPhoto?
    suspend fun changePrimaryMember(memberId: String)
    suspend fun deleteMember(memberId: String)
    suspend fun uploadPhoto(photo: PreparedFamilyPhoto, message: String): SharedFamilyPhoto
    suspend fun deletePhoto(photoId: String)
}
