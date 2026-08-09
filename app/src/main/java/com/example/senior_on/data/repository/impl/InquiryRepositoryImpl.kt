package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.InquiryAnswerResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.inquiry.InquiryDataSource
import com.example.senior_on.domain.model.inquiry.InquiryAnswer
import com.example.senior_on.domain.model.inquiry.InquiryDetail
import com.example.senior_on.domain.model.inquiry.InquiryStatus
import com.example.senior_on.domain.model.inquiry.InquirySummary
import com.example.senior_on.domain.repository.inquiry.InquiryRepository

class InquiryRepositoryImpl(
    private val dataSource: InquiryDataSource,
) : InquiryRepository {
    override suspend fun getInquiries(): List<InquirySummary> =
        dataSource.getInquiries().mapNotNull { it.toSummaryOrNull() }

    override suspend fun getInquiry(inquiryId: Long): InquiryDetail =
        dataSource.getInquiry(inquiryId).toDetail()

    private fun InquiryListItemResponse.toSummaryOrNull(): InquirySummary? {
        val mappedStatus = status.toInquiryStatusOrNull() ?: return null
        return InquirySummary(
            id = inquiryId,
            title = title.orEmpty(),
            status = mappedStatus,
            createdAt = createdAt.orEmpty(),
        )
    }

    private fun InquiryDetailResponse.toDetail(): InquiryDetail {
        val mappedStatus = status.toInquiryStatusOrNull()
            ?: InquiryStatus.Waiting
        return InquiryDetail(
            id = inquiryId,
            title = title.orEmpty(),
            content = content.orEmpty(),
            status = mappedStatus,
            createdAt = createdAt.orEmpty(),
            imageUrls = images.orEmpty().filter { it.isNotBlank() },
            answers = answers.orEmpty().map { it.toDomain() },
        )
    }

    private fun InquiryAnswerResponse.toDomain(): InquiryAnswer =
        InquiryAnswer(
            id = answerId,
            content = content.orEmpty(),
            createdAt = createdAt.orEmpty(),
        )

    private fun String?.toInquiryStatusOrNull(): InquiryStatus? = when (this?.trim()?.uppercase()) {
        "WAITING" -> InquiryStatus.Waiting
        "COMPLETED" -> InquiryStatus.Completed
        else -> null
    }
}
