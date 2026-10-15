package com.example.senior_on.data.source.family

import com.example.senior_on.data.remote.api.FamilyApi
import com.example.senior_on.data.remote.api.FamilyPhotoStorageApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import com.example.senior_on.data.source.remoteRequest
import okhttp3.RequestBody
import retrofit2.HttpException

interface RemoteFamilySource {
    suspend fun join(request: FamilyJoinRequest): FamilyJoinResponse
    suspend fun createCode(): FamilyCodeCreateResponse
    suspend fun getCode(seniorId: Long): FamilyCodeResponse
    suspend fun getHome(seniorId: Long): FamilyHomeResponse
    suspend fun getMembers(seniorId: Long): List<FamilyMemberResponse>
    suspend fun changePrimaryManager(
        seniorId: Long,
        request: FamilyPrimaryManagerUpdateRequest,
    ): FamilyPrimaryManagerUpdateResponse
    suspend fun deleteMember(userId: Long, seniorId: Long)
    suspend fun getPhotoGroupConnections(
        seniorId: Long,
    ): List<FamilyPhotoGroupConnectionResponse> = emptyList()
    suspend fun connectPhotoGroup(request: FamilyPhotoGroupConnectionRequest) {
        error("Photo-group connection is not implemented")
    }
    suspend fun disconnectPhotoGroup(seniorId: Long, photoGroupId: Long) {
        error("Photo-group disconnection is not implemented")
    }
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
}

class RemoteFamilyDataSource(
    private val api: FamilyApi,
    private val storageApi: FamilyPhotoStorageApi,
) : RemoteFamilySource {
    override suspend fun join(request: FamilyJoinRequest) = remoteRequest {
        api.join(request).requireData()
    }
    override suspend fun createCode() = remoteRequest {
        api.createCode().requireData()
    }
    override suspend fun getCode(seniorId: Long) = remoteRequest {
        api.getCode(seniorId).requireData()
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
    override suspend fun getPhotoGroupConnections(seniorId: Long) =
        api.getPhotoGroupConnections(seniorId).requireData()
    override suspend fun connectPhotoGroup(request: FamilyPhotoGroupConnectionRequest) {
        remoteRequest { api.connectPhotoGroup(request); Unit }
    }
    override suspend fun disconnectPhotoGroup(seniorId: Long, photoGroupId: Long) {
        remoteRequest {
            api.disconnectPhotoGroup(photoGroupId = photoGroupId, seniorId = seniorId)
            Unit
        }
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
}
