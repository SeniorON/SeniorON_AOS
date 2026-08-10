package com.example.senior_on.data.source.notification

import com.example.senior_on.data.remote.api.NotificationApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface NotificationDataSource {
    suspend fun getNotifications(type: String, cursor: Long?, size: Int?): NotificationListResponse
    suspend fun markRead(id: Long)
    suspend fun delete(id: Long)
    suspend fun getSettings(): NotificationHomeListResponse
    suspend fun updateSetting(type: String, request: NotificationSettingRequest): NotificationSettingResponse
    suspend fun getParentDeviceStatus(): ParentDeviceStatusResponse
    suspend fun getInactivitySetting(userId: Long): InactivitySettingResponse
    suspend fun getMyInactivitySetting(): InactivitySettingResponse
    suspend fun updateInactivitySetting(userId: Long, request: InactivitySettingRequest): InactivitySettingResponse
}

class RemoteNotificationDataSource(private val api: NotificationApi) : NotificationDataSource {
    override suspend fun getNotifications(type: String, cursor: Long?, size: Int?) =
        api.getNotifications(type, cursor, size).requireData()
    override suspend fun markRead(id: Long) { api.markRead(id) }
    override suspend fun delete(id: Long) { api.delete(id) }
    override suspend fun getSettings() = api.getSettings().requireData()
    override suspend fun updateSetting(type: String, request: NotificationSettingRequest) =
        api.updateSetting(type, request).requireData()
    override suspend fun getParentDeviceStatus() = api.getParentDeviceStatus().requireData()
    override suspend fun getInactivitySetting(userId: Long) =
        api.getInactivitySetting(userId).requireData()
    override suspend fun getMyInactivitySetting() =
        api.getMyInactivitySetting().requireData()
    override suspend fun updateInactivitySetting(userId: Long, request: InactivitySettingRequest) =
        api.updateInactivitySetting(userId, request).requireData()
}
