package com.example.senior_on.data.source.family

import com.example.senior_on.data.remote.api.FamilyApi
import com.example.senior_on.data.remote.api.FamilyPhotoUploadApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import com.example.senior_on.data.source.remoteRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface RemoteFamilySource {
    suspend fun join(request: FamilyJoinRequest): FamilyJoinResponse
    suspend fun createCode(): FamilyCodeCreateResponse
    suspend fun getCode(): FamilyCodeResponse
    suspend fun getHome(): FamilyHomeResponse
    suspend fun getMembers(): List<FamilyMemberResponse>
    suspend fun changePrimaryManager(request: FamilyPrimaryManagerUpdateRequest): FamilyPrimaryManagerUpdateResponse
    suspend fun deleteMember(userId: Long)
    suspend fun getPhotos(uploaderId: Long?, cursorAt: String?, cursorId: Long?, size: Int?): FamilyPhotoListResponse
    suspend fun uploadPhoto(
        idempotencyKey: String,
        image: MultipartBody.Part,
        description: RequestBody?,
    ): FamilyPhotoItemResponse
    suspend fun getPhoto(photoId: Long): FamilyPhotoItemResponse
    suspend fun getAlbums(): List<FamilyPhotoAlbumResponse>
    suspend fun markViewed(photoId: Long)
    suspend fun deletePhoto(photoId: Long)
}

class RemoteFamilyDataSource(
    private val api: FamilyApi,
    private val uploadApi: FamilyPhotoUploadApi,
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
    override suspend fun getMembers() = api.getMembers().requireData()
    override suspend fun changePrimaryManager(request: FamilyPrimaryManagerUpdateRequest) =
        api.changePrimaryManager(request).requireData()
    override suspend fun deleteMember(userId: Long) { api.deleteMember(userId) }
    override suspend fun getPhotos(uploaderId: Long?, cursorAt: String?, cursorId: Long?, size: Int?) =
        api.getPhotos(uploaderId, cursorAt, cursorId, size).requireData()
    override suspend fun uploadPhoto(
        idempotencyKey: String,
        image: MultipartBody.Part,
        description: RequestBody?,
    ) = uploadApi.uploadPhoto(idempotencyKey, image, description).requireData()
    override suspend fun getPhoto(photoId: Long) = api.getPhoto(photoId).requireData()
    override suspend fun getAlbums() = api.getAlbums().requireData()
    override suspend fun markViewed(photoId: Long) { api.markViewed(photoId) }
    override suspend fun deletePhoto(photoId: Long) { api.deletePhoto(photoId) }
}
