package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.api.ParentSettingsApi
import com.example.senior_on.data.remote.api.SeniorPermissionUpdate
import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.data.source.requireData
import retrofit2.HttpException

class ParentSettingsRepository(private val api: ParentSettingsApi) {
    suspend fun getPermissions(seniorId: Long) = remoteRequest {
        api.getPermissions(seniorId).requireData()
    }

    suspend fun updatePermissions(location: Boolean? = null, inactivity: Boolean? = null) = remoteRequest {
        api.updatePermissions(SeniorPermissionUpdate(location, inactivity)).requireData()
    }

    suspend fun disconnect() = remoteRequest {
        val response = api.disconnect()
        if (!response.isSuccessful) throw HttpException(response)
    }
}
