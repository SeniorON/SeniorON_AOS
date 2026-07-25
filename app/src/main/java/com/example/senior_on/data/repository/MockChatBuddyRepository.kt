package com.example.senior_on.data.repository

import kotlinx.coroutines.delay

class MockChatBuddyRepository : ChatBuddyRepository {
    private val replies = listOf(
        "오늘도 좋은 하루예요. 지금 기분은 어떠세요?",
        "그렇군요. 천천히 말씀해 주셔도 괜찮아요.",
        "말씀해 주셔서 고마워요. 제가 계속 들어드릴게요."
    )

    override suspend fun requestReply(message: String, turn: Int): String {
        delay(1_100)
        return replies[turn % replies.size]
    }
}
