package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.ChatBuddyAudio
import com.example.senior_on.domain.model.parent.ChatBuddyConversation
import com.example.senior_on.domain.model.parent.ChatBuddyConversationEnd
import com.example.senior_on.domain.model.parent.ChatBuddyVoiceTurn
import com.example.senior_on.domain.model.parent.ParentFamilyPhotoCollection
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.domain.model.parent.ParentLinkSafetyResult
import kotlinx.coroutines.flow.StateFlow

interface CaregiverRelationshipDataSource {
    val relationship: StateFlow<CaregiverRelationship?>
    fun saveRelationship(seniorId: Long, relationship: CaregiverRelationship)
}

interface ChatBuddyDataSource {
    suspend fun startConversation(): ChatBuddyConversation
    suspend fun sendVoiceTurn(
        conversationId: Long,
        requestId: String,
        audio: ChatBuddyAudio,
    ): ChatBuddyVoiceTurn
    suspend fun endConversation(conversationId: Long): ChatBuddyConversationEnd
}

interface ParentFamilyPhotoDataSource {
    suspend fun getFamilyPhotos(familyCode: String): List<ParentFamilyPhotoCollection>
}

interface ParentInfoDataSource {
    val parentInfo: StateFlow<ParentInfo?>
    fun saveParentInfo(parentInfo: ParentInfo)
    fun clearParentInfo()
}

interface ParentLinkSafetyDataSource {
    suspend fun inspectLink(url: String): ParentLinkSafetyResult
}
