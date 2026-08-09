package com.example.senior_on.domain.repository.inquiry

import com.example.senior_on.domain.model.inquiry.InquiryCreateResult
import com.example.senior_on.domain.model.inquiry.InquiryDetail
import com.example.senior_on.domain.model.inquiry.InquirySummary

interface InquiryRepository {
    suspend fun getInquiries(): List<InquirySummary>

    suspend fun getInquiry(inquiryId: Long): InquiryDetail

    suspend fun createInquiry(
        title: String,
        content: String,
        imageUris: List<String> = emptyList(),
    ): InquiryCreateResult
}
