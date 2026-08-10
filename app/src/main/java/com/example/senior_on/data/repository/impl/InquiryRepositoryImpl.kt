package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.remote.dto.InquiryAnswerResponse
import com.example.senior_on.data.remote.dto.InquiryCreateRequest
import com.example.senior_on.data.remote.dto.InquiryCreateResponse
import com.example.senior_on.data.remote.dto.InquiryDetailResponse
import com.example.senior_on.data.remote.dto.InquiryListItemResponse
import com.example.senior_on.data.source.inquiry.InquiryDataSource
import com.example.senior_on.domain.model.family.PreparedFamilyPhoto
import com.example.senior_on.domain.model.inquiry.InquiryAnswer
import com.example.senior_on.domain.model.inquiry.InquiryCreateResult
import com.example.senior_on.domain.model.inquiry.InquiryDetail
import com.example.senior_on.domain.model.inquiry.InquiryStatus
import com.example.senior_on.domain.model.inquiry.InquirySummary
import com.example.senior_on.domain.repository.inquiry.InquiryRepository
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class InquiryRepositoryImpl(
    private val dataSource: InquiryDataSource,
    private val photoUploadPreparer: FamilyPhotoUploadPreparer,
    private val gson: Gson = Gson(),
) : InquiryRepository {
    override suspend fun getInquiries(): List<InquirySummary> =
        dataSource.getInquiries().map { it.toSummary() }

    override suspend fun getInquiry(inquiryId: Long): InquiryDetail =
        dataSource.getInquiry(inquiryId).toDetail()

    override suspend fun createInquiry(
        title: String,
        content: String,
        imageUris: List<String>,
    ): InquiryCreateResult {
        val preparedImages = mutableListOf<PreparedFamilyPhoto>()
        return try {
            imageUris
                .distinct()
                .take(MaxInquiryImages)
                .forEach { uri ->
                    preparedImages += photoUploadPreparer.prepare(uri)
                }
            val requestJson = gson.toJson(
                InquiryCreateRequest(
                    title = title.trim(),
                    content = content.trim(),
                )
            )
            val requestBody = requestJson.toRequestBody(JsonMediaType)
            val imageParts = preparedImages.map { photo ->
                MultipartBody.Part.createFormData(
                    "images",
                    photo.displayName,
                    photo.file.asRequestBody(photo.mimeType.toMediaType()),
                )
            }
            dataSource.createInquiry(
                request = requestBody,
                images = imageParts,
            ).toCreateResult()
        } finally {
            preparedImages.forEach { photo ->
                runCatching { photo.file.delete() }
            }
        }
    }

    private fun InquiryListItemResponse.toSummary(): InquirySummary =
        InquirySummary(
            id = inquiryId,
            title = title.orEmpty(),
            status = status.toInquiryStatus(),
            createdAt = createdAt.orEmpty(),
        )

    private fun InquiryDetailResponse.toDetail(): InquiryDetail =
        InquiryDetail(
            id = inquiryId,
            title = title.orEmpty(),
            content = content.orEmpty(),
            status = status.toInquiryStatus(),
            createdAt = createdAt.orEmpty(),
            imageUrls = images.orEmpty().filter { it.isNotBlank() },
            answers = answers.orEmpty().map { it.toDomain() },
        )

    private fun InquiryCreateResponse.toCreateResult(): InquiryCreateResult =
        InquiryCreateResult(
            id = inquiryId,
            title = title.orEmpty(),
            status = status.toInquiryStatus(),
            createdAt = createdAt.orEmpty(),
            imageUrls = imageUrls.orEmpty().filter { it.isNotBlank() },
        )

    private fun InquiryAnswerResponse.toDomain(): InquiryAnswer =
        InquiryAnswer(
            id = answerId,
            content = content.orEmpty(),
            createdAt = createdAt.orEmpty(),
        )

    private fun String?.toInquiryStatus(): InquiryStatus = when (this?.trim()?.uppercase()) {
        "WAITING" -> InquiryStatus.Waiting
        "COMPLETED" -> InquiryStatus.Completed
        else -> InquiryStatus.Unknown
    }

    private companion object {
        const val MaxInquiryImages = 5
        val JsonMediaType = "application/json; charset=utf-8".toMediaType()
    }
}
