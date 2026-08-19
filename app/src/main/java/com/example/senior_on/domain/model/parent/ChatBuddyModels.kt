package com.example.senior_on.domain.model.parent

data class ChatBuddyAudio(
    val fileName: String,
    val contentType: String = M4A_CONTENT_TYPE,
    val bytes: ByteArray,
) {
    init {
        require(fileName.isNotBlank())
        require(bytes.isNotEmpty())
    }

    companion object {
        const val M4A_CONTENT_TYPE = "audio/mp4"
    }
}

data class ChatBuddyConversation(
    val id: Long,
    val status: ChatBuddyConversationStatus,
    val startedAt: String,
    val newlyCreated: Boolean,
)

data class ChatBuddyVoiceTurn(
    val conversationId: Long,
    val turnId: Long,
    val status: ChatBuddyTurnStatus,
    val outcome: ChatBuddyTurnOutcome,
    val failureStage: ChatBuddyFailureStage?,
    val transcript: String,
    val assistantText: String,
    val safetyType: ChatBuddySafetyType?,
    val audioContentType: String?,
    val audioFormat: String?,
    val audioBytes: ByteArray?,
)

data class ChatBuddyConversationEnd(
    val conversationId: Long,
    val status: ChatBuddyConversationStatus,
    val endedAt: String?,
)

enum class ChatBuddyConversationStatus { ACTIVE, ENDED, UNKNOWN }
enum class ChatBuddyTurnStatus { RECEIVED, TRANSCRIBED, RESPONSE_GENERATED, COMPLETED, FAILED, UNKNOWN }
enum class ChatBuddyTurnOutcome { SUCCESS, FALLBACK, PROCESSING, UNKNOWN }
enum class ChatBuddyFailureStage { STT, SAFETY, LLM, TTS, PERSISTENCE, UNKNOWN }
enum class ChatBuddySafetyType { NORMAL, EMERGENCY, UNKNOWN }
