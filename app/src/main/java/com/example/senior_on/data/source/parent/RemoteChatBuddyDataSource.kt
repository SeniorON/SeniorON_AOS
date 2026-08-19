package com.example.senior_on.data.source.parent

import android.util.Base64
import com.example.senior_on.data.remote.api.CompanionApi
import com.example.senior_on.data.remote.dto.CompanionConversationEndResponse
import com.example.senior_on.data.remote.dto.CompanionConversationStartResponse
import com.example.senior_on.data.remote.dto.CompanionVoiceTurnResponse
import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.data.source.requireData
import com.example.senior_on.domain.model.parent.ChatBuddyAudio
import com.example.senior_on.domain.model.parent.ChatBuddyConversation
import com.example.senior_on.domain.model.parent.ChatBuddyConversationEnd
import com.example.senior_on.domain.model.parent.ChatBuddyConversationStatus
import com.example.senior_on.domain.model.parent.ChatBuddyFailureStage
import com.example.senior_on.domain.model.parent.ChatBuddySafetyType
import com.example.senior_on.domain.model.parent.ChatBuddyTurnOutcome
import com.example.senior_on.domain.model.parent.ChatBuddyTurnStatus
import com.example.senior_on.domain.model.parent.ChatBuddyVoiceTurn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteChatBuddyDataSource(
    private val api: CompanionApi,
) : ChatBuddyDataSource {
    override suspend fun startConversation(): ChatBuddyConversation = remoteRequest {
        api.startConversation().requireData().toDomain()
    }

    override suspend fun sendVoiceTurn(
        conversationId: Long,
        requestId: String,
        audio: ChatBuddyAudio,
    ): ChatBuddyVoiceTurn = remoteRequest {
        val body = audio.bytes.toRequestBody(audio.contentType.toMediaType())
        val part = MultipartBody.Part.createFormData("audio", audio.fileName, body)
        api.sendVoiceTurn(conversationId, requestId, part).requireData().toDomain()
    }

    override suspend fun endConversation(
        conversationId: Long,
    ): ChatBuddyConversationEnd = remoteRequest {
        api.endConversation(conversationId).requireData().toDomain()
    }
}

private fun CompanionConversationStartResponse.toDomain() = ChatBuddyConversation(
    id = conversationId,
    status = conversationStatus.toConversationStatus(),
    startedAt = startedAt,
    newlyCreated = created,
)

private fun CompanionVoiceTurnResponse.toDomain() = ChatBuddyVoiceTurn(
    conversationId = conversationId,
    turnId = turnId,
    status = turnStatus.toTurnStatus(),
    outcome = outcome.toTurnOutcome(),
    failureStage = failureStage?.toFailureStage(),
    transcript = transcript.orEmpty(),
    assistantText = assistantText.orEmpty(),
    safetyType = safetyType?.toSafetyType(),
    audioContentType = audioContentType,
    audioFormat = audioFormat,
    audioBytes = audioBase64
        ?.takeIf(String::isNotBlank)
        ?.let { encoded -> Base64.decode(encoded, Base64.DEFAULT) },
)

private fun CompanionConversationEndResponse.toDomain() = ChatBuddyConversationEnd(
    conversationId = conversationId,
    status = conversationStatus.toConversationStatus(),
    endedAt = endedAt,
)

private fun String.toConversationStatus() = enumValueOrUnknown(
    value = this,
    unknown = ChatBuddyConversationStatus.UNKNOWN,
)

private fun String.toTurnStatus() = enumValueOrUnknown(
    value = this,
    unknown = ChatBuddyTurnStatus.UNKNOWN,
)

private fun String.toTurnOutcome() = enumValueOrUnknown(
    value = this,
    unknown = ChatBuddyTurnOutcome.UNKNOWN,
)

private fun String.toFailureStage() = enumValueOrUnknown(
    value = this,
    unknown = ChatBuddyFailureStage.UNKNOWN,
)

private fun String.toSafetyType() = enumValueOrUnknown(
    value = this,
    unknown = ChatBuddySafetyType.UNKNOWN,
)

private inline fun <reified T : Enum<T>> enumValueOrUnknown(
    value: String,
    unknown: T,
): T = enumValues<T>().firstOrNull { it.name == value } ?: unknown
