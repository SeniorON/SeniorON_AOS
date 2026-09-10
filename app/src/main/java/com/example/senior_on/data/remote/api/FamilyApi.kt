package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FamilyApi {
    @POST("api/family/join") suspend fun join(@Body request: FamilyJoinRequest): ApiResponse<FamilyJoinResponse>
    @POST("api/family/code-create") suspend fun createCode(): ApiResponse<FamilyCodeCreateResponse>
    @GET("api/family/code") suspend fun getCode(): ApiResponse<FamilyCodeResponse>
    @GET("api/family/home") suspend fun getHome(): ApiResponse<FamilyHomeResponse>
    @GET("api/family/members") suspend fun getMembers(): ApiResponse<List<FamilyMemberResponse>>
    @PATCH("api/family/primary-manager")
    suspend fun changePrimaryManager(@Body request: FamilyPrimaryManagerUpdateRequest): ApiResponse<FamilyPrimaryManagerUpdateResponse>
    @DELETE("api/family/members/{targetUserId}")
    suspend fun deleteMember(@Path("targetUserId") targetUserId: Long): ApiResponse<Unit>
    @GET("api/family/photos") suspend fun getPhotos(
        @Query("uploaderUserId") uploaderUserId: Long? = null,
        @Query("cursorCreatedAt") cursorCreatedAt: String? = null,
        @Query("cursorId") cursorId: Long? = null,
        @Query("size") size: Int? = null
    ): ApiResponse<FamilyPhotoListResponse>
    @GET("api/family/photos/{familyPhotoId}")
    suspend fun getPhoto(
        @Path("familyPhotoId") familyPhotoId: Long,
    ): ApiResponse<FamilyPhotoItemResponse>
    @GET("api/family/photos/albums")
    suspend fun getAlbums(): ApiResponse<List<FamilyPhotoAlbumResponse>>
    @PATCH("api/family/photos/{familyPhotoId}/viewed")
    suspend fun markViewed(@Path("familyPhotoId") familyPhotoId: Long): ApiResponse<Unit>
    @DELETE("api/family/photos/{familyPhotoId}")
    suspend fun deletePhoto(@Path("familyPhotoId") familyPhotoId: Long): ApiResponse<Unit>
    @POST("api/family/photos/upload-url")
    suspend fun createPhotoUploadUrl(
        @Body request: FamilyPhotoUploadUrlRequest,
    ): ApiResponse<FamilyPhotoUploadUrlResponse>
    @POST("api/family/photos/complete")
    suspend fun completePhotoUpload(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: FamilyPhotoUploadCompleteRequest,
    ): ApiResponse<FamilyPhotoItemResponse>
}

interface FamilyPhotoStorageApi {
    @PUT
    suspend fun uploadPhoto(
        @Url uploadUrl: String,
        @Body image: RequestBody,
    ): Response<Unit>
}
