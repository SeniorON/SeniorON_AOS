package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import retrofit2.http.GET

interface InquiryApi {
    @GET("api/inquiries")
    suspend fun getInquiries(): ApiResponse<List<InquiryListItemResponse>>
}
