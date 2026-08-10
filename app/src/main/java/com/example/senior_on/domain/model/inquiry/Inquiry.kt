package com.example.senior_on.domain.model.inquiry

enum class InquiryStatus {
    Waiting,
    Completed,
    Unknown,
}

data class InquirySummary(
    val id: Long,
    val title: String,
    val status: InquiryStatus,
    val createdAt: String,
)

data class InquiryAnswer(
    val id: Long,
    val content: String,
    val createdAt: String,
)

data class InquiryDetail(
    val id: Long,
    val title: String,
    val content: String,
    val status: InquiryStatus,
    val createdAt: String,
    val imageUrls: List<String>,
    val answers: List<InquiryAnswer>,
)

data class InquiryCreateResult(
    val id: Long,
    val title: String,
    val status: InquiryStatus,
    val createdAt: String,
    val imageUrls: List<String>,
)
