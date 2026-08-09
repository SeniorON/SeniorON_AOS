package com.example.senior_on.data.source.inquiry

import com.example.senior_on.data.remote.api.InquiryApi
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.requireData

interface InquiryDataSource {
    suspend fun getInquiries(): List<InquiryListItemResponse>
}

class RemoteInquiryDataSource(
    private val api: InquiryApi,
) : InquiryDataSource {
    override suspend fun getInquiries(): List<InquiryListItemResponse> =
        api.getInquiries().requireData()
}
