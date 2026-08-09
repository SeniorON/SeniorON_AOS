package com.example.senior_on.ui.child.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
)

data class InquiryUiState(
    val historyItems: List<InquiryHistoryUiItem> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyErrorMessage: String? = null,
)

class InquiryViewModel(
    private val inquiryRepository: InquiryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InquiryUiState())
    val uiState: StateFlow<InquiryUiState> = _uiState.asStateFlow()

    fun loadInquiries() {
        if (_uiState.value.isHistoryLoading) return
        viewModelScope.launch {
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
                        historyItems = inquiries.map { summary -> summary.toUiItem() },
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

    fun consumeHistoryError() {
        _uiState.update { it.copy(historyErrorMessage = null) }
    }

    private fun InquirySummary.toUiItem(): InquiryHistoryUiItem =
        InquiryHistoryUiItem(
            id = id.toString(),
            isAnswered = status == InquiryStatus.Completed,
            createdAtLabel = formatCreatedAt(createdAt),
            question = title,
        )

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
