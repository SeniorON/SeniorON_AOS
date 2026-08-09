package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.inquiry.InquiryDetail
import com.example.senior_on.domain.model.inquiry.InquiryStatus
import com.example.senior_on.domain.model.inquiry.InquirySummary
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InquiryHistoryUiItem(
    val id: String,
    val isAnswered: Boolean,
    val createdAtLabel: String,
    val question: String,
    val answer: String? = null,
    val isDetailLoading: Boolean = false,
)

data class InquiryUiState(
    val historyItems: List<InquiryHistoryUiItem> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyErrorMessage: String? = null,
    val detailErrorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null,
    val submitCompleted: Boolean = false,
)

class InquiryViewModel(
    private val inquiryRepository: InquiryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InquiryUiState())
    val uiState: StateFlow<InquiryUiState> = _uiState.asStateFlow()

    private val detailLoadingIds = mutableSetOf<String>()

    fun loadInquiries() {
        if (_uiState.value.isHistoryLoading) return
        viewModelScope.launch {
            val previousAnswers = _uiState.value.historyItems
                .associate { it.id to it.answer }
            _uiState.update {
                it.copy(
                    isHistoryLoading = true,
                    historyErrorMessage = null,
                )
            }
            runCatching {
                inquiryRepository.getInquiries()
            }.onSuccess { inquiries ->
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        historyItems = inquiries.map { summary ->
                            summary.toUiItem(previousAnswer = previousAnswers[summary.id.toString()])
                        },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        historyErrorMessage = throwable.message
                            ?: "문의 목록을 불러오지 못했습니다.",
                    )
                }
            }
        }
    }

    fun loadInquiryDetail(inquiryId: String) {
        val current = _uiState.value.historyItems.firstOrNull { it.id == inquiryId } ?: return
        if (!current.isAnswered) return
        if (!current.answer.isNullOrBlank()) return
        if (!detailLoadingIds.add(inquiryId)) return

        val parsedId = inquiryId.toLongOrNull() ?: run {
            detailLoadingIds.remove(inquiryId)
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    historyItems = state.historyItems.map { item ->
                        if (item.id == inquiryId) item.copy(isDetailLoading = true) else item
                    },
                    detailErrorMessage = null,
                )
            }
            runCatching {
                inquiryRepository.getInquiry(parsedId)
            }.onSuccess { detail ->
                _uiState.update { state ->
                    state.copy(
                        historyItems = state.historyItems.map { item ->
                            if (item.id == inquiryId) {
                                item.copy(
                                    answer = detail.toAnswerText(),
                                    question = detail.title.ifBlank { item.question },
                                    isDetailLoading = false,
                                )
                            } else {
                                item
                            }
                        },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        historyItems = state.historyItems.map { item ->
                            if (item.id == inquiryId) {
                                item.copy(isDetailLoading = false)
                            } else {
                                item
                            }
                        },
                        detailErrorMessage = throwable.message
                            ?: "문의 상세를 불러오지 못했습니다.",
                    )
                }
            }
            detailLoadingIds.remove(inquiryId)
        }
    }

    fun submitInquiry(
        title: String,
        content: String,
        imageUris: List<String>,
    ) {
        if (_uiState.value.isSubmitting) return
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isBlank() || trimmedContent.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    submitErrorMessage = null,
                    submitCompleted = false,
                )
            }
            runCatching {
                inquiryRepository.createInquiry(
                    title = trimmedTitle,
                    content = trimmedContent,
                    imageUris = imageUris,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitCompleted = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitErrorMessage = throwable.message
                            ?: "문의 등록에 실패했습니다.",
                    )
                }
            }
        }
    }

    fun consumeHistoryError() {
        _uiState.update { it.copy(historyErrorMessage = null) }
    }

    fun consumeDetailError() {
        _uiState.update { it.copy(detailErrorMessage = null) }
    }

    fun consumeSubmitError() {
        _uiState.update { it.copy(submitErrorMessage = null) }
    }

    fun consumeSubmitCompleted() {
        _uiState.update { it.copy(submitCompleted = false) }
    }

    private fun InquirySummary.toUiItem(previousAnswer: String?): InquiryHistoryUiItem =
        InquiryHistoryUiItem(
            id = id.toString(),
            isAnswered = status == InquiryStatus.Completed,
            createdAtLabel = formatCreatedAt(createdAt),
            question = title,
            answer = previousAnswer,
        )

    private fun InquiryDetail.toAnswerText(): String? {
        val joined = answers
            .map { it.content.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "\n\n")
        return joined.ifBlank { null }
    }

    private fun formatCreatedAt(raw: String): String {
        if (raw.isBlank()) return raw
        val dateTime = runCatching { OffsetDateTime.parse(raw) }.getOrNull()?.toLocalDateTime()
            ?: runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDateTime() }
                .getOrNull()
            ?: runCatching { LocalDateTime.parse(raw) }.getOrNull()
            ?: return raw
        return dateTime.format(InquiryDateTimeFormatter)
    }

    companion object {
        private val InquiryDateTimeFormatter =
            DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")

        fun factory(
            inquiryRepository: InquiryRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                InquiryViewModel(inquiryRepository) as T
        }
    }
}
