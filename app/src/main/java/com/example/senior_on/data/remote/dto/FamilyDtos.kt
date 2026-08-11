package com.example.senior_on.data.remote.dto

data class FamilyJoinRequest(val familyCode: String)
data class FamilyJoinResponse(val familyId: Long?, val familyCode: String?)
data class FamilyCodeCreateResponse(val familyId: Long?, val familyCode: String?)
data class FamilyPrimaryManagerUpdateRequest(val targetUserId: Long)
data class FamilyPrimaryManagerUpdateResponse(
    val usersId: Long?, val name: String?, val managerType: String?
)
data class FamilyPhotoCreateRequest(val image: String, val description: String?)
data class FamilyPhotoItemResponse(
    val familyPhotoId: Long?, val imageUrl: String?, val uploaderUserId: Long?,
    val uploaderName: String?, val description: String?, val canDelete: Boolean?,
    val createdAt: String?, val newPhoto: Boolean?
)
data class FamilyPhotoCursorResponse(val createdAt: String?, val familyPhotoId: Long?)
data class FamilyPhotoListResponse(
    val photos: List<FamilyPhotoItemResponse>?, val totalCount: Long?,
    val nextCursor: FamilyPhotoCursorResponse?, val hasNext: Boolean?
)
data class FamilyPhotoAlbumResponse(
    val uploaderUserId: Long?, val uploaderName: String?, val latestPhotoUrl: String?,
    val photoCount: Long?, val hasNewPhotos: Boolean?
)
data class FamilyMemberResponse(
    val usersId: Long?, val name: String?, val role: String?,
    val canBecomePrimary: Boolean?, val managerType: String?, val me: Boolean?,
    val profileImageUrl: String?
)
data class FamilyHomeResponse(
    val members: List<FamilyMemberResponse>?,
    val recentUploaderProfileImageUrls: List<String>?,
    val recentPhotos: List<FamilyPhotoItemResponse>?
)
data class FamilyCodeResponse(val familyCode: String?, val familyMemberCount: Long?)
