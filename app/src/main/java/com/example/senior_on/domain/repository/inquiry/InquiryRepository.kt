package com.example.senior_on.domain.repository.inquiry

import com.example.senior_on.domain.model.inquiry.InquirySummary

interface InquiryRepository {
    suspend fun getInquiries(): List<InquirySummary>
}
