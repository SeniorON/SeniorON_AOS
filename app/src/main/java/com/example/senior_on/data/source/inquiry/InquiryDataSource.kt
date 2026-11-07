package com.example.senior_on.data.source.inquiry

import com.example.senior_on.data.remote.api.InquiryApi
import com.example.senior_on.data.remote.dto.InquiryCreateResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.requireData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.senior_on.domain.repository.inquiry.InquiryResultUnknownException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

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
    ): InquiryCreateResponse = try {
        // A multipart body with a one-shot part must not be replayed by OkHttp.
        val oneShot = object : RequestBody() {
            override fun contentType() = request.contentType()
            override fun contentLength() = request.contentLength()
            override fun writeTo(sink: okio.BufferedSink) = request.writeTo(sink)
            override fun isOneShot() = true
        }
        api.createInquiry(oneShot, images.ifEmpty { null }).requireData()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (e is HttpException && e.code() in 400..499 && e.code() != 408) throw e
        throw InquiryResultUnknownException(e)
    }
}
