package com.example.senior_on.ui.child.settings.viewmodel

import com.example.senior_on.logging.AppLog as Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.senior_on.domain.model.inquiry.InquiryDetail
import com.example.senior_on.domain.model.inquiry.InquiryStatus
import com.example.senior_on.domain.model.inquiry.InquirySummary
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import com.example.senior_on.domain.repository.inquiry.InquiryResultUnknownException
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
import kotlinx.coroutines.CancellationException

data class InquiryHistoryUiItem(
    val id: String,
    val isAnswered: Boolean,
    val createdAtLabel: String,
    val question: String,
    val answer: String? = null,
    val isDetailLoading: Boolean = false,
)

data class InquiryDraft(val title: String = "", val content: String = "", val images: List<String> = emptyList())
enum class InquiryPage { Write, History }
enum class InquiryNotice(val message: String, val action: String, val page: InquiryPage) {
    Success("문의가 등록됐어요.", "내역 보기", InquiryPage.History),
    Failed("문의를 등록하지 못했어요.", "작성 화면으로", InquiryPage.Write),
    Unknown("등록 여부를 확인하지 못했어요.", "내 문의내역 확인", InquiryPage.History),
}

data class InquiryUiState(
    val draft: InquiryDraft = InquiryDraft(),
    val needsHistoryCheck: Boolean = false,
    val notice: InquiryNotice? = null,
    val openPage: InquiryPage? = null,
    val historyItems: List<InquiryHistoryUiItem> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyErrorMessage: String? = null,
    val detailErrorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitCompleted: Boolean = false,
)

class InquiryViewModel(
    private val inquiryRepository: InquiryRepository,
    private val logFailure: (String, Throwable) -> Unit = { message, cause -> Log.e(Tag, message, cause) },
) : ViewModel() {
    private val _uiState = MutableStateFlow(InquiryUiState())
    val uiState: StateFlow<InquiryUiState> = _uiState.asStateFlow()

    private val detailLoadingIds = mutableSetOf<String>()
    private var writeVisible = false
    private var submissionNumber = 0L

    fun setWriteVisible(visible: Boolean) {
        writeVisible = visible
        if (!visible && _uiState.value.submitCompleted) {
            _uiState.update { it.copy(submitCompleted = false, notice = InquiryNotice.Success) }
        }
    }

    fun editDraft(draft: InquiryDraft) {
        _uiState.update { if (it.isSubmitting) it else it.copy(draft = draft) }
    }

    fun openPage(page: InquiryPage) = _uiState.update { it.copy(openPage = page) }
    fun consumePage() = _uiState.update { it.copy(openPage = null) }
    fun consumeNotice(notice: InquiryNotice) = _uiState.update {
        if (it.notice == notice) it.copy(notice = null) else it
    }

    fun loadInquiries() {
        if (_uiState.value.isHistoryLoading) return
        val checkSubmission = submissionNumber.takeIf { _uiState.value.needsHistoryCheck }
        _uiState.update { it.copy(isHistoryLoading = true, historyErrorMessage = null) }
        viewModelScope.launch {
            runCatching {
                inquiryRepository.getInquiries()
            }.onSuccess { inquiries ->
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        needsHistoryCheck = it.needsHistoryCheck && checkSubmission != submissionNumber,
                        historyItems = inquiries.map { summary -> summary.toUiItem() },
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                logFailure("Failed to load inquiries", throwable)
                _uiState.update {
                    it.copy(
                        isHistoryLoading = false,
                        historyErrorMessage = "문의 목록을 불러오지 못했습니다.",
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
            try {
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
                    if (throwable is CancellationException) throw throwable
                    logFailure("Failed to load inquiry detail", throwable)
                    _uiState.update { state ->
                        state.copy(
                            historyItems = state.historyItems.map { item ->
                                if (item.id == inquiryId) {
                                    item.copy(isDetailLoading = false)
                                } else {
                                    item
                                }
                            },
                            detailErrorMessage = "문의 상세를 불러오지 못했습니다.",
                        )
                    }
                }
            } finally {
                detailLoadingIds.remove(inquiryId)
            }
        }
    }

    fun submitInquiry(
        title: String,
        content: String,
        imageUris: List<String>,
    ) {
        if (_uiState.value.isSubmitting || _uiState.value.submitCompleted || _uiState.value.needsHistoryCheck) return
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isBlank() || trimmedContent.isBlank()) return

        submissionNumber++
        _uiState.update { it.copy(isSubmitting = true, notice = null,
            draft = InquiryDraft(title, content, imageUris.toList())) }
        viewModelScope.launch {
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
                        draft = InquiryDraft(),
                        submitCompleted = writeVisible,
                        notice = if (writeVisible) null else InquiryNotice.Success,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                logFailure("Failed to submit inquiry", throwable)
                val uncertain = throwable is InquiryResultUnknownException
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        needsHistoryCheck = uncertain,
                        notice = if (uncertain) InquiryNotice.Unknown else InquiryNotice.Failed,
                    )
                }
            }
        }
    }

    fun consumeDetailError() {
        _uiState.update { it.copy(detailErrorMessage = null) }
    }

    fun consumeSubmitCompleted() {
        _uiState.update { it.copy(submitCompleted = false) }
    }

    private fun InquirySummary.toUiItem(): InquiryHistoryUiItem =
        InquiryHistoryUiItem(
            id = id.toString(),
            isAnswered = status == InquiryStatus.Completed,
            createdAtLabel = formatCreatedAt(createdAt),
            question = title,
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
        private const val Tag = "InquiryViewModel"
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
