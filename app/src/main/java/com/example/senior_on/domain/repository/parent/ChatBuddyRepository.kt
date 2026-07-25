package com.example.senior_on.domain.repository.parent

interface ChatBuddyRepository {
    suspend fun requestReply(message: String, turn: Int): String
}
