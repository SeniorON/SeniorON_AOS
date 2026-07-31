package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.parent.CaregiverRelationshipDataSource
import com.example.senior_on.data.source.parent.ChatBuddyDataSource
import com.example.senior_on.data.source.parent.ParentFamilyPhotoDataSource
import com.example.senior_on.data.source.parent.ParentInfoDataSource
import com.example.senior_on.data.source.parent.ParentLinkSafetyDataSource
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.ParentFamilyPhotoCollection
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult
import com.example.senior_on.domain.repository.parent.CaregiverRelationshipRepository
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.domain.repository.parent.ParentInfoRepository
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import kotlinx.coroutines.flow.StateFlow

class CaregiverRelationshipRepositoryImpl(
    private val dataSource: CaregiverRelationshipDataSource
) : CaregiverRelationshipRepository {
    override val relationship: StateFlow<CaregiverRelationship?> = dataSource.relationship

    override fun saveRelationship(
        seniorId: Long,
        relationship: CaregiverRelationship
    ) = dataSource.saveRelationship(seniorId, relationship)
}

class ChatBuddyRepositoryImpl(
    private val dataSource: ChatBuddyDataSource
) : ChatBuddyRepository {
    override suspend fun requestReply(message: String, turn: Int): String =
        dataSource.requestReply(message.trim(), turn.coerceAtLeast(0))
}

class ParentFamilyPhotoRepositoryImpl(
    private val dataSource: ParentFamilyPhotoDataSource
) : ParentFamilyPhotoRepository {
    override suspend fun getFamilyPhotos(
        familyCode: String
    ): List<ParentFamilyPhotoCollection> =
        dataSource.getFamilyPhotos(familyCode.trim())
}

class ParentInfoRepositoryImpl(
    private val dataSource: ParentInfoDataSource
) : ParentInfoRepository {
    override val parentInfo: StateFlow<ParentInfo?> = dataSource.parentInfo
    override fun saveParentInfo(parentInfo: ParentInfo) = dataSource.saveParentInfo(parentInfo)
}

class ParentLinkSafetyRepositoryImpl(
    private val dataSource: ParentLinkSafetyDataSource
) : ParentLinkSafetyRepository {
    override suspend fun inspectLink(url: String): ParentLinkSafetyResult =
        dataSource.inspectLink(url.trim())
}

