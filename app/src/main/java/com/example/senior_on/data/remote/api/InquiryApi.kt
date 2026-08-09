package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface InquiryApi {
    @GET("api/inquiries")
    suspend fun getInquiries(): ApiResponse<List<InquiryListItemResponse>>

    @GET("api/inquiries/{inquiryId}")
    suspend fun getInquiry(
        @Path("inquiryId") inquiryId: Long,
    ): ApiResponse<InquiryDetailResponse>
}
