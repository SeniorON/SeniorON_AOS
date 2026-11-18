package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.local.AccessTokenStore

import com.example.senior_on.data.remote.api.DeviceApi
import com.example.senior_on.data.source.remoteRequest
import retrofit2.HttpException

enum class ParentConnectionDestination { FirstConnection, Reconnect, Home }

class ParentReconnectionRepository(private val api: DeviceApi) {
    suspend fun destination(): ParentConnectionDestination = remoteRequest {
        val scope = AccessTokenStore.homeLayoutScope()
        val status = api.getReconnection()
        val family = requireNotNull(status.familyConnected) { "가족 연결 상태를 확인하지 못했어요." }
        val disconnected = requireNotNull(status.deviceDisconnected) { "기기 연결 상태를 확인하지 못했어요." }
        if (!family || disconnected) AccessTokenStore.invalidateHomeLayout(scope)
        when {
            !family -> ParentConnectionDestination.FirstConnection
            disconnected -> ParentConnectionDestination.Reconnect
            else -> ParentConnectionDestination.Home
        }
    }

    suspend fun reconnect() = remoteRequest {
        val response = api.reconnect()
        if (!response.isSuccessful) throw HttpException(response)
    }
}
