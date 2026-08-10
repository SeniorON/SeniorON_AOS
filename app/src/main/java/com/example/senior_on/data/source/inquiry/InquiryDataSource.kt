package com.example.senior_on.data.source.inquiry

import com.example.senior_on.data.remote.api.InquiryApi
import com.example.senior_on.data.remote.dto.InquiryCreateResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.requireData
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface InquiryDataSource {
    suspend fun getInquiries(): List<InquiryListItemResponse>

    suspend fun getInquiry(inquiryId: Long): InquiryDetailResponse

    suspend fun createInquiry(
        request: RequestBody,
        images: List<MultipartBody.Part>,
    ): InquiryCreateResponse
}

class RemoteInquiryDataSource(
    private val api: InquiryApi,
) : InquiryDataSource {
    override suspend fun getInquiries(): List<InquiryListItemResponse> =
        api.getInquiries().requireData()

    override suspend fun getInquiry(inquiryId: Long): InquiryDetailResponse =
        api.getInquiry(inquiryId).requireData()

    override suspend fun createInquiry(
        request: RequestBody,
        images: List<MultipartBody.Part>,
    ): InquiryCreateResponse = api.createInquiry(
        request = request,
        images = images.ifEmpty { null },
    ).requireData()
}
