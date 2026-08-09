package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.inquiry.InquiryDataSource
import com.example.senior_on.domain.model.inquiry.InquiryStatus
import com.example.senior_on.domain.model.inquiry.InquirySummary
import com.example.senior_on.domain.repository.inquiry.InquiryRepository

class InquiryRepositoryImpl(
    private val dataSource: InquiryDataSource,
) : InquiryRepository {
    override suspend fun getInquiries(): List<InquirySummary> =
        dataSource.getInquiries().mapNotNull { it.toDomainOrNull() }

    private fun InquiryListItemResponse.toDomainOrNull(): InquirySummary? {
        val id = inquiryId
        val mappedStatus = status.toInquiryStatusOrNull() ?: return null
        return InquirySummary(
            id = id,
            title = title.orEmpty(),
            status = mappedStatus,
            createdAt = createdAt.orEmpty(),
        )
    }

    private fun String?.toInquiryStatusOrNull(): InquiryStatus? = when (this?.trim()?.uppercase()) {
        "WAITING" -> InquiryStatus.Waiting
        "COMPLETED" -> InquiryStatus.Completed
        else -> null
    }
}
