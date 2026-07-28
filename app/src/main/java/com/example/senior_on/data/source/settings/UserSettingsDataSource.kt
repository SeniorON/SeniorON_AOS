package com.example.senior_on.data.source.settings

import com.example.senior_on.data.remote.api.UserSettingsApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import okhttp3.MultipartBody

interface UserSettingsDataSource {
    suspend fun getName(): CurrentNameResponse
    suspend fun updateName(request: NameUpdateRequest): NameUpdateResponse
    suspend fun changePassword(request: PasswordChangeRequest): PasswordChangeResponse
    suspend fun getProfileImage(): ProfileImageResponse
    suspend fun updateProfileImage(image: MultipartBody.Part): ProfileImageUpdateResponse
}

class RemoteUserSettingsDataSource(private val api: UserSettingsApi) : UserSettingsDataSource {
    override suspend fun getName() = api.getName().requireData()
    override suspend fun updateName(request: NameUpdateRequest) = api.updateName(request).requireData()
    override suspend fun changePassword(request: PasswordChangeRequest) =
        api.changePassword(request).requireData()
    override suspend fun getProfileImage() = api.getProfileImage().requireData()
    override suspend fun updateProfileImage(image: MultipartBody.Part) =
        api.updateProfileImage(image).requireData()
}
