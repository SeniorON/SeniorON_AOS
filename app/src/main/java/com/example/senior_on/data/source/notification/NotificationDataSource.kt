package com.example.senior_on.data.source.notification

import com.example.senior_on.data.remote.api.NotificationApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface NotificationDataSource {
    suspend fun getNotifications(seniorId: Long, type: String, cursor: Long?, size: Int?): NotificationListResponse
    suspend fun markRead(id: Long)
    suspend fun delete(id: Long)
    suspend fun getSettings(seniorId: Long): NotificationHomeListResponse
    suspend fun updateSetting(seniorId: Long, type: String, request: NotificationSettingRequest): NotificationSettingResponse
    suspend fun getParentDeviceStatus(seniorId: Long): ParentDeviceStatusResponse
    suspend fun getInactivitySetting(userId: Long): InactivitySettingResponse
    suspend fun getMyInactivitySetting(): InactivitySettingResponse
    suspend fun updateInactivitySetting(userId: Long, request: InactivitySettingRequest): InactivitySettingResponse
}

class RemoteNotificationDataSource(private val api: NotificationApi) : NotificationDataSource {
    override suspend fun getNotifications(seniorId: Long, type: String, cursor: Long?, size: Int?) =
        api.getNotifications(seniorId, type, cursor, size).requireData()
    override suspend fun markRead(id: Long) { api.markRead(id) }
    override suspend fun delete(id: Long) { api.delete(id) }
    override suspend fun getSettings(seniorId: Long) = api.getSettings(seniorId).requireData()
    override suspend fun updateSetting(seniorId: Long, type: String, request: NotificationSettingRequest) =
        api.updateSetting(type = type, seniorId = seniorId, request = request).requireData()
    override suspend fun getParentDeviceStatus(seniorId: Long) = api.getParentDeviceStatus(seniorId).requireData()
    override suspend fun getInactivitySetting(userId: Long) =
        api.getInactivitySetting(userId).requireData()
    override suspend fun getMyInactivitySetting() =
        api.getMyInactivitySetting().requireData()
    override suspend fun updateInactivitySetting(userId: Long, request: InactivitySettingRequest) =
        api.updateInactivitySetting(userId, request).requireData()
}
