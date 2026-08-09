package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.InquiryCreateResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface InquiryApi {
    @GET("api/inquiries")
    suspend fun getInquiries(): ApiResponse<List<InquiryListItemResponse>>

    @GET("api/inquiries/{inquiryId}")
    suspend fun getInquiry(
        @Path("inquiryId") inquiryId: Long,
    ): ApiResponse<InquiryDetailResponse>

    @Multipart
    @POST("api/inquiries")
    suspend fun createInquiry(
        @Part("request") request: RequestBody,
        @Part images: List<MultipartBody.Part>?,
    ): ApiResponse<InquiryCreateResponse>
}
