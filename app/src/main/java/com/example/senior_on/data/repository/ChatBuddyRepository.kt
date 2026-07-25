package com.example.senior_on.data.repository

interface ChatBuddyRepository {
    suspend fun requestReply(message: String, turn: Int): String
}
