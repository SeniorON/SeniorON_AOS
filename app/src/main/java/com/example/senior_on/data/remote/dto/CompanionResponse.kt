package com.example.senior_on.data.remote.dto

data class CompanionConversationStartResponse(
    val conversationId: Long,
    val conversationStatus: String,
    val startedAt: String,
    val created: Boolean,
)

data class CompanionVoiceTurnResponse(
    val conversationId: Long,
    val turnId: Long,
    val turnStatus: String,
    val outcome: String,
    val failureStage: String?,
    val transcript: String?,
    val assistantText: String?,
    val safetyType: String?,
    val audioContentType: String?,
    val audioFormat: String?,
    val audioBase64: String?,
)

data class CompanionConversationEndResponse(
    val conversationId: Long,
    val conversationStatus: String,
    val endedAt: String?,
)
