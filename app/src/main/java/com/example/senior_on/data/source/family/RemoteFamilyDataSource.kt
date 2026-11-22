package com.example.senior_on.data.source.family

import com.example.senior_on.data.remote.api.FamilyApi
import com.example.senior_on.data.remote.api.FamilyPhotoStorageApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.domain.model.family.FamilyPhotoReportResult
import com.example.senior_on.domain.model.auth.RemoteRequestException
import okhttp3.RequestBody
import retrofit2.HttpException

interface RemoteFamilySource {
    suspend fun disconnectFamily(familyId: Long): FamilyConnectionDisconnectResponse = error("가족 연결 해제 서비스를 사용할 수 없어요.")
    suspend fun join(request: FamilyJoinRequest): FamilyJoinResponse
    suspend fun createCode(): FamilyCodeCreateResponse
    suspend fun getCode(familyId: Long): FamilyCodeResponse
    suspend fun getHome(seniorId: Long): FamilyHomeResponse
    suspend fun getMembers(seniorId: Long): List<FamilyMemberResponse>
    suspend fun changePrimaryManager(
        seniorId: Long,
        request: FamilyPrimaryManagerUpdateRequest,
    ): FamilyPrimaryManagerUpdateResponse
    suspend fun deleteMember(userId: Long, seniorId: Long)
    suspend fun getPhotoTargets(): List<FamilyPhotoTargetResponse> = emptyList()
    suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
        seniorId: Long,
    ): FamilyPhotoListResponse
    suspend fun createPhotoUploadUrl(
        request: FamilyPhotoUploadUrlRequest,
    ): FamilyPhotoUploadUrlResponse
    suspend fun uploadPhotoToStorage(
        uploadUrl: String,
        image: RequestBody,
    )
    suspend fun completePhotoUpload(
        idempotencyKey: String,
        request: FamilyPhotoUploadCompleteRequest,
    ): FamilyPhotoItemResponse
    suspend fun getPhoto(photoId: Long): FamilyPhotoItemResponse
    suspend fun getAlbums(seniorId: Long): List<FamilyPhotoAlbumResponse>
    suspend fun markViewed(photoId: Long, seniorId: Long)
    suspend fun deletePhoto(photoId: Long)
    suspend fun reportPhoto(photoId: Long, request: FamilyPhotoReportRequest): FamilyPhotoReportResult {
        error("Family photo reporting is not implemented")
    }
    suspend fun reportUploader(photoId: Long, request: FamilyUserReportRequest): FamilyPhotoReportResult {
        error("Family photo uploader reporting is not implemented")
    }
}

internal suspend fun reportResult(request: suspend () -> ApiResponse<Unit>): FamilyPhotoReportResult {
    val response = try {
        remoteRequest(request)
    } catch (error: RemoteRequestException) {
        if (error.code !in setOf("FAMILY_PHOTO409_1", "FAMILY_PHOTO409_2")) throw error
        return FamilyPhotoReportResult.Duplicate(error.message.orEmpty(), hidden = true)
    }
    return when (response.code) {
        "COMMON_200" -> FamilyPhotoReportResult.Created
        "FAMILY_PHOTO200_1", "FAMILY_PHOTO200_3" -> FamilyPhotoReportResult.Duplicate(response.message, hidden = false)
        "FAMILY_PHOTO200_2", "FAMILY_PHOTO200_4" -> FamilyPhotoReportResult.Duplicate(response.message, hidden = true)
        else -> error("신고 결과를 확인하지 못했어요.")
    }
}

class RemoteFamilyDataSource(
    private val api: FamilyApi,
    private val storageApi: FamilyPhotoStorageApi,
) : RemoteFamilySource {
    override suspend fun disconnectFamily(familyId: Long) = remoteRequest {
        api.disconnectFamily(familyId).requireData()
    }
    override suspend fun join(request: FamilyJoinRequest) = remoteRequest {
        api.join(request).requireData()
    }
    override suspend fun createCode() = remoteRequest {
        api.createCode().requireData()
    }
    override suspend fun getCode(familyId: Long) = remoteRequest {
        api.getCode(familyId).requireData()
    }
    override suspend fun getHome(seniorId: Long) = remoteRequest {
        api.getHome(seniorId).requireData()
    }
    override suspend fun getMembers(seniorId: Long) = remoteRequest {
        api.getMembers(seniorId).requireData()
    }
    override suspend fun changePrimaryManager(
        seniorId: Long,
        request: FamilyPrimaryManagerUpdateRequest,
    ) = remoteRequest {
        api.changePrimaryManager(seniorId, request).requireData()
    }
    override suspend fun deleteMember(userId: Long, seniorId: Long) {
        remoteRequest { api.deleteMember(userId, seniorId); Unit }
    }
    override suspend fun getPhotoTargets() = remoteRequest {
        api.getPhotoTargets().requireData()
    }
    override suspend fun getPhotos(
        uploaderId: Long?,
        cursorAt: String?,
        cursorId: Long?,
        size: Int?,
        seniorId: Long,
    ) = remoteRequest {
        api.getPhotos(
            seniorId = seniorId,
            uploaderUserId = uploaderId,
            cursorCreatedAt = cursorAt,
            cursorId = cursorId,
            size = size,
        ).requireData()
    }
    override suspend fun createPhotoUploadUrl(
        request: FamilyPhotoUploadUrlRequest,
    ) = api.createPhotoUploadUrl(request).requireData()
    override suspend fun uploadPhotoToStorage(
        uploadUrl: String,
        image: RequestBody,
    ) {
        val response = storageApi.uploadPhoto(uploadUrl, image)
        if (!response.isSuccessful) throw HttpException(response)
    }
    override suspend fun completePhotoUpload(
        idempotencyKey: String,
        request: FamilyPhotoUploadCompleteRequest,
    ) = api.completePhotoUpload(idempotencyKey, request).requireData()
    override suspend fun getPhoto(photoId: Long) = api.getPhoto(photoId).requireData()
    override suspend fun getAlbums(seniorId: Long) = api.getAlbums(seniorId).requireData()
    override suspend fun markViewed(photoId: Long, seniorId: Long) { api.markViewed(photoId, seniorId) }
    override suspend fun deletePhoto(photoId: Long) { api.deletePhoto(photoId) }
    override suspend fun reportPhoto(photoId: Long, request: FamilyPhotoReportRequest) = reportResult { api.reportPhoto(photoId, request) }
    override suspend fun reportUploader(photoId: Long, request: FamilyUserReportRequest) = reportResult { api.reportUploader(photoId, request) }
}
