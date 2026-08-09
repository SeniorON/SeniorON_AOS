package com.example.senior_on.domain.model.inquiry

enum class InquiryStatus {
    Waiting,
    Completed,
}

data class InquirySummary(
    val id: Long,
    val title: String,
    val status: InquiryStatus,
    val createdAt: String,
)
