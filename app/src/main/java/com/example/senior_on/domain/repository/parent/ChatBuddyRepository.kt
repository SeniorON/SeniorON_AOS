package com.example.senior_on.domain.repository.parent

import com.example.senior_on.domain.model.parent.ChatBuddyAudio
import com.example.senior_on.domain.model.parent.ChatBuddyConversation
import com.example.senior_on.domain.model.parent.ChatBuddyConversationEnd
import com.example.senior_on.domain.model.parent.ChatBuddyVoiceTurn

interface ChatBuddyRepository {
    suspend fun startConversation(): ChatBuddyConversation
    suspend fun sendVoiceTurn(
        conversationId: Long,
        requestId: String,
        audio: ChatBuddyAudio,
    ): ChatBuddyVoiceTurn
    suspend fun endConversation(conversationId: Long): ChatBuddyConversationEnd
}
