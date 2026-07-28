package com.example.senior_on.data.remote.api

import com.example.senior_on.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserSettingsApi {
    @GET("api/users/settings/name") suspend fun getName(): ApiResponse<CurrentNameResponse>
    @PATCH("api/users/settings/name") suspend fun updateName(@Body request: NameUpdateRequest): ApiResponse<NameUpdateResponse>
    @PATCH("api/users/settings/password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): ApiResponse<PasswordChangeResponse>
    @GET("api/users/settings/profile-image")
    suspend fun getProfileImage(): ApiResponse<ProfileImageResponse>
    @Multipart @PATCH("api/users/settings/profile-image")
    suspend fun updateProfileImage(@Part image: MultipartBody.Part): ApiResponse<ProfileImageUpdateResponse>
}
