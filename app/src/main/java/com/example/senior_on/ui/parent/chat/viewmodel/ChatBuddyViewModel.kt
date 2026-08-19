package com.example.senior_on.ui.parent.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.model.parent.ChatBuddyAudio
import com.example.senior_on.domain.model.parent.ChatBuddySafetyType
import com.example.senior_on.domain.model.parent.ChatBuddyTurnOutcome
import com.example.senior_on.domain.model.parent.ChatBuddyVoiceTurn
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChatBuddyPhase {
    Idle,
    Listening,
    Thinking,
    Responding,
}

data class ChatBuddyUiState(
    val phase: ChatBuddyPhase = ChatBuddyPhase.Idle,
    val conversationId: Long? = null,
    val recognizedText: String = "",
    val replyText: String = "",
    val responseAudio: ByteArray? = null,
    val responseAudioContentType: String? = null,
    val responseAudioFormat: String? = null,
    val safetyType: ChatBuddySafetyType? = null,
    val completedTurns: Int = 0,
    val isStartingConversation: Boolean = false,
    val errorMessage: String? = null,
)

class ChatBuddyViewModel(
    private val repository: ChatBuddyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatBuddyUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startConversation()
    }

    fun onRecordingStarted() {
        if (_uiState.value.conversationId == null) {
            if (!_uiState.value.isStartingConversation) startConversation()
            return
        }
        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Listening,
                recognizedText = "",
                replyText = "",
                responseAudio = null,
                responseAudioContentType = null,
                responseAudioFormat = null,
                safetyType = null,
                errorMessage = null,
            )
        }
    }

    fun submitRecordedAudio(audio: ChatBuddyAudio) {
        if (_uiState.value.phase == ChatBuddyPhase.Thinking) return

        viewModelScope.launch {
            val conversationId = ensureConversation() ?: return@launch
            val requestId = UUID.randomUUID().toString()

            _uiState.update {
                it.copy(
                    phase = ChatBuddyPhase.Thinking,
                    errorMessage = null,
                    responseAudio = null,

                    responseAudioContentType = null,
                    responseAudioFormat = null,
                )
            }

            try {
                val turn = sendUntilReady(
                    conversationId = conversationId,
                    requestId = requestId,
                    audio = audio,
                )
                applyTurn(turn)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        phase = ChatBuddyPhase.Idle,
                        errorMessage = exception.message
                            ?: "답변을 불러오지 못했어요. 다시 말씀해 주세요.",
                    )
                }
            }
        }
    }

    fun onResponsePlaybackCompleted() {
        if (_uiState.value.phase != ChatBuddyPhase.Responding) return
        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Idle,
                completedTurns = it.completedTurns + 1,
                responseAudio = null,
                responseAudioContentType = null,
                responseAudioFormat = null,
            )
        }
    }

    fun onMicrophonePermissionDenied() {
        onAudioFailure("말벗을 이용하려면 마이크 권한을 허용해 주세요.")
    }

    fun onAudioFailure(message: String) {
        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Idle,
                responseAudio = null,
                responseAudioContentType = null,
                responseAudioFormat = null,
                errorMessage = message,
            )
        }
    }

    fun onResponsePlaybackFailed() {
        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Idle,
                completedTurns = it.completedTurns + 1,
                responseAudio = null,
                responseAudioContentType = null,
                responseAudioFormat = null,
                errorMessage = "답변 음성을 재생하지 못했어요. 화면의 답변을 확인해 주세요.",
            )
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun endConversation(onFinished: () -> Unit) {
        val conversationId = _uiState.value.conversationId
        if (conversationId == null) {
            onFinished()
            return
        }

        viewModelScope.launch {
            try {
                repository.endConversation(conversationId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // 종료 실패가 사용자의 화면 이탈을 막지 않도록 best effort로 처리합니다.
            } finally {
                onFinished()
            }
        }
    }

    private fun startConversation() {
        if (_uiState.value.isStartingConversation || _uiState.value.conversationId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isStartingConversation = true, errorMessage = null) }
            try {
                val conversation = repository.startConversation()
                _uiState.update {
                    it.copy(
                        conversationId = conversation.id,
                        isStartingConversation = false,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isStartingConversation = false,
                        errorMessage = exception.message ?: "말벗 대화를 시작하지 못했어요.",
                    )
                }
            }
        }
    }

    private suspend fun ensureConversation(): Long? {
        _uiState.value.conversationId?.let { return it }

        return try {
            val conversation = repository.startConversation()
            _uiState.update {
                it.copy(
                    conversationId = conversation.id,
                    isStartingConversation = false,
                )
            }
            conversation.id
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.update {
                it.copy(
                    phase = ChatBuddyPhase.Idle,
                    isStartingConversation = false,
                    errorMessage = exception.message ?: "말벗 대화를 시작하지 못했어요.",
                )
            }
            null
        }
    }

    private suspend fun sendUntilReady(
        conversationId: Long,
        requestId: String,
        audio: ChatBuddyAudio,
    ): ChatBuddyVoiceTurn {
        repeat(PROCESSING_RETRY_COUNT) { attempt ->
            val turn = repository.sendVoiceTurn(conversationId, requestId, audio)
            if (turn.outcome != ChatBuddyTurnOutcome.PROCESSING) return turn
            if (attempt < PROCESSING_RETRY_COUNT - 1) delay(PROCESSING_RETRY_DELAY_MILLIS)
        }
        error("답변을 준비하고 있어요. 잠시 후 다시 시도해 주세요.")
    }

    private fun applyTurn(turn: ChatBuddyVoiceTurn) {
        _uiState.update {
            val hasAudioResponse = turn.audioBytes != null
            it.copy(
                phase = if (hasAudioResponse) ChatBuddyPhase.Responding else ChatBuddyPhase.Idle,
                recognizedText = turn.transcript,
                replyText = turn.assistantText,
                responseAudio = turn.audioBytes,
                responseAudioContentType = turn.audioContentType,
                responseAudioFormat = turn.audioFormat,
                safetyType = turn.safetyType,
                completedTurns = if (hasAudioResponse) it.completedTurns else it.completedTurns + 1,
                errorMessage = null,
            )
        }
    }

    companion object {
        private const val PROCESSING_RETRY_COUNT = 3
        private const val PROCESSING_RETRY_DELAY_MILLIS = 1_000L

        fun factory(repository: ChatBuddyRepository) = viewModelFactory {
            initializer {
                ChatBuddyViewModel(repository)
            }
        }
    }
}
