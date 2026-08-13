package com.example.senior_on.data.source.settings

import com.example.senior_on.data.remote.api.UserSettingsApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import com.example.senior_on.data.source.remoteRequest
import okhttp3.MultipartBody

interface UserSettingsDataSource {
    suspend fun getAccount(): UserAccountResponse
    suspend fun updateName(request: NameUpdateRequest): NameUpdateResponse
    suspend fun changePassword(request: PasswordChangeRequest): PasswordChangeResponse
    suspend fun getProfileImage(): ProfileImageResponse
    suspend fun resetProfileImage(): ProfileImageResponse
    suspend fun updateProfileImage(image: MultipartBody.Part): ProfileImageUpdateResponse
}

class RemoteUserSettingsDataSource(private val api: UserSettingsApi) : UserSettingsDataSource {
    override suspend fun getAccount() = remoteRequest {
        api.getAccount().requireData()
    }

    override suspend fun updateName(request: NameUpdateRequest) = remoteRequest {
        api.updateName(request).requireData()
    }

    override suspend fun changePassword(request: PasswordChangeRequest) = remoteRequest {
        api.changePassword(request).requireData()
    }

    override suspend fun getProfileImage() = remoteRequest {
        api.getProfileImage().requireData()
    }

    override suspend fun resetProfileImage() = remoteRequest {
        api.resetProfileImage().requireData()
    }

    override suspend fun updateProfileImage(image: MultipartBody.Part) = remoteRequest {
        api.updateProfileImage(image).requireData()
    }
}
