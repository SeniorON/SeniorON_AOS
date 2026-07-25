package com.example.senior_on.ui.parent.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChatBuddyPhase {
    Idle,
    Listening,
    Thinking,
    Responding
}

data class ChatBuddyUiState(
    val phase: ChatBuddyPhase = ChatBuddyPhase.Idle,
    val recognizedText: String = "",
    val replyText: String = "",
    val completedTurns: Int = 0
)

class ChatBuddyViewModel(
    private val repository: ChatBuddyRepository
) : ViewModel() {
    private val mockRecognizedMessages = listOf(
        "오늘 날씨가 참 좋네요.",
        "어제 가족과 통화해서 기분이 좋아요.",
        "오늘 오후에는 무엇을 하면 좋을까요?"
    )

    private val _uiState = MutableStateFlow(ChatBuddyUiState())
    val uiState = _uiState.asStateFlow()

    private var recognitionJob: Job? = null
    private var responseJob: Job? = null

    fun onVoiceButtonClick() {
        when (_uiState.value.phase) {
            ChatBuddyPhase.Idle -> startMockRecognition()
            ChatBuddyPhase.Listening -> finishRecognitionAndRequestReply()
            ChatBuddyPhase.Thinking,
            ChatBuddyPhase.Responding -> Unit
        }
    }

    private fun startMockRecognition() {
        responseJob?.cancel()
        val turn = _uiState.value.completedTurns
        val message = mockRecognizedMessages[turn % mockRecognizedMessages.size]

        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Listening,
                recognizedText = "",
                replyText = ""
            )
        }

        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            message.forEachIndexed { index, _ ->
                delay(105)
                _uiState.update {
                    it.copy(recognizedText = message.take(index + 1))
                }
            }
        }
    }

    private fun finishRecognitionAndRequestReply() {
        recognitionJob?.cancel()

        val state = _uiState.value
        val turn = state.completedTurns
        val fallbackMessage =
            mockRecognizedMessages[turn % mockRecognizedMessages.size]
        val message = state.recognizedText.ifBlank { fallbackMessage }

        _uiState.update {
            it.copy(
                phase = ChatBuddyPhase.Thinking,
                recognizedText = message,
                replyText = ""
            )
        }

        responseJob?.cancel()
        responseJob = viewModelScope.launch {
            try {
                val reply = repository.requestReply(message, turn)
                _uiState.update { it.copy(phase = ChatBuddyPhase.Responding) }

                reply.forEachIndexed { index, _ ->
                    delay(55)
                    _uiState.update {
                        it.copy(replyText = reply.take(index + 1))
                    }
                }

                delay(900)
                _uiState.update {
                    it.copy(
                        phase = ChatBuddyPhase.Idle,
                        completedTurns = it.completedTurns + 1
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        phase = ChatBuddyPhase.Idle,
                        replyText = "잠시 후 다시 말씀해 주세요."
                    )
                }
            }
        }
    }

    override fun onCleared() {
        recognitionJob?.cancel()
        responseJob?.cancel()
        super.onCleared()
    }

    companion object {
        fun factory(repository: ChatBuddyRepository) = viewModelFactory {
            initializer {
                ChatBuddyViewModel(repository)
            }
        }
    }
}
