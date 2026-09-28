package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.api.ParentSettingsApi
import com.example.senior_on.data.remote.api.SeniorPermissionUpdate
import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.data.source.requireData
import retrofit2.HttpException

class ParentSettingsRepository(
    private val api: ParentSettingsApi,
    private val sharingGuard: ParentSharingGuard? = null,
    private val onLocationDisabled: () -> Unit = {},
) {
    suspend fun getPermissions(seniorId: Long) = remoteRequest {
        api.getPermissions(seniorId).requireData()
    }

    suspend fun updatePermissions(location: Boolean? = null, inactivity: Boolean? = null) = remoteRequest {
        val request = SeniorPermissionUpdate(location, inactivity)
        val saved = sharingGuard?.update(request) ?: api.updatePermissions(request).requireData()
        if (!saved.locationEnabled) onLocationDisabled()
        saved
    }

    suspend fun disconnect() = remoteRequest {
        val gate = com.example.senior_on.data.local.ParentConnectionGate
        val wasReady = gate.isReady()
        val version = gate.hold()
        try {
            val response = api.disconnect()
            if (!response.isSuccessful) throw HttpException(response)
        } catch (error: Exception) {
            if (wasReady) gate.approve(version)
            throw error
        }
    }
}
