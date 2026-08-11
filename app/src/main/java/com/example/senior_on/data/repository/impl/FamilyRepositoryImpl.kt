package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.family.FamilyDataSource
import com.example.senior_on.domain.model.family.FamilyJoinResult
import com.example.senior_on.domain.model.family.FamilyOverview
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.family.SharedFamilyPhoto
import com.example.senior_on.domain.repository.family.FamilyRepository
import kotlinx.coroutines.flow.Flow

class FamilyRepositoryImpl(
    private val dataSource: FamilyDataSource
) : FamilyRepository {
    override suspend fun joinFamily(familyCode: String): FamilyJoinResult =
        dataSource.joinFamily(normalizeFamilyCodeForRequest(familyCode))

    override fun observeFamilyOverview(): Flow<FamilyOverview> =
        dataSource.observeFamilyOverview()

    override suspend fun getFamilyOverview(): FamilyOverview =
        dataSource.getFamilyOverview()

    override suspend fun refreshFamilyOverview() =
        dataSource.refreshFamilyOverview()

    override suspend fun getSharedPhoto(photoId: String): SharedFamilyPhoto? =
        dataSource.getSharedPhoto(photoId.trim())

    override suspend fun changePrimaryMember(memberId: String) =
        dataSource.changePrimaryMember(memberId.trim())

    override suspend fun deleteMember(memberId: String) =
        dataSource.deleteMember(memberId.trim())

    override suspend fun uploadPhoto(
        photo: PreparedFamilyPhoto,
        message: String
    ): SharedFamilyPhoto = dataSource.uploadPhoto(photo, message.trim())

    override suspend fun deletePhoto(photoId: String) =
        dataSource.deletePhoto(photoId.trim())
}
