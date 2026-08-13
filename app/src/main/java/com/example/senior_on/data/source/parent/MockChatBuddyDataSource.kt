package com.example.senior_on.data.source.parent

import kotlinx.coroutines.delay

/**
 * 말벗 기능은 프론트엔드 API 연동이 아직 완료되지 않아 임시 응답을 사용합니다.
 * 현재는 화면 흐름 확인을 위한 구현이며, 연동 완료 시 원격 DataSource로 교체합니다.
 */
class MockChatBuddyDataSource : ChatBuddyDataSource {
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
