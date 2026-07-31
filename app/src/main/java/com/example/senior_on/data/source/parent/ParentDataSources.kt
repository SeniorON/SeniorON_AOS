package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.ParentFamilyPhotoCollection
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult
import com.example.senior_on.domain.model.parent.ParentMedication
import kotlinx.coroutines.flow.StateFlow

interface CaregiverRelationshipDataSource {
    val relationship: StateFlow<CaregiverRelationship?>
    fun saveRelationship(seniorId: Long, relationship: CaregiverRelationship)
}

interface ChatBuddyDataSource {
    suspend fun requestReply(message: String, turn: Int): String
}

interface ParentFamilyPhotoDataSource {
    suspend fun getFamilyPhotos(familyCode: String): List<ParentFamilyPhotoCollection>
}

interface ParentInfoDataSource {
    val parentInfo: StateFlow<ParentInfo?>
    fun saveParentInfo(parentInfo: ParentInfo)
}

interface ParentLinkSafetyDataSource {
    suspend fun inspectLink(url: String): ParentLinkSafetyResult
}

interface ParentMedicationDataSource {
    suspend fun getTodayMedication(): ParentMedication?
    suspend fun markAsTaken(medicationId: String): ParentMedication
}

