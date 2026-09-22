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
    suspend fun getCode(): FamilyCodeResponse
    suspend fun getHome(): FamilyHomeResponse
    suspend fun getMembers(seniorId: Long? = null): List<FamilyMemberResponse>
    suspend fun changePrimaryManager(request: FamilyPrimaryManagerUpdateRequest): FamilyPrimaryManagerUpdateResponse
    suspend fun deleteMember(userId: Long)
    suspend fun getPhotos(uploaderId: Long?, cursorAt: String?, cursorId: Long?, size: Int?, seniorId: Long? = null): FamilyPhotoListResponse
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
    override suspend fun getCode() = remoteRequest {
        api.getCode().requireData()
    }
    override suspend fun getHome() = api.getHome().requireData()
    override suspend fun getMembers(seniorId: Long?) =
        api.getMembers(seniorId).requireData()
    override suspend fun changePrimaryManager(request: FamilyPrimaryManagerUpdateRequest) =
        api.changePrimaryManager(request).requireData()
    override suspend fun deleteMember(userId: Long) { api.deleteMember(userId) }
    override suspend fun getPhotos(uploaderId: Long?, cursorAt: String?, cursorId: Long?, size: Int?, seniorId: Long?) =
        api.getPhotos(uploaderId, cursorAt, cursorId, size, seniorId).requireData()
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
