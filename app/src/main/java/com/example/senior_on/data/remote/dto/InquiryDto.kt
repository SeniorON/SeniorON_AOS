package com.example.senior_on.data.remote.dto

data class InquiryListItemResponse(
    val inquiryId: Long,
    val title: String?,
    val status: String?,
    val createdAt: String?,
)

data class InquiryDetailResponse(
    val inquiryId: Long,
    val title: String?,
    val content: String?,
    val status: String?,
    val createdAt: String?,
    val images: List<String>? = null,
    val answers: List<InquiryAnswerResponse>? = null,
)

data class InquiryAnswerResponse(
    val answerId: Long,
    val content: String?,
    val createdAt: String?,
)

data class InquiryCreateRequest(
    val title: String,
    val content: String,
)

data class InquiryCreateResponse(
    val inquiryId: Long,
    val title: String?,
    val status: String?,
    val createdAt: String?,
    val imageUrls: List<String>? = null,
)
