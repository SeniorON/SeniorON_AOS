package com.example.senior_on.data.source.medication

import com.example.senior_on.data.remote.api.MedicationApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface MedicationDataSource {
    suspend fun getMedications(parentId: Long): List<MedicationReadResponse>
    suspend fun create(parentId: Long, request: MedicationCreateRequest): MedicationCreateResponse
    suspend fun update(parentId: Long, request: MedicationUpdateRequest): String
    suspend fun delete(parentId: Long, groupId: String): String
    suspend fun getMySchedules(date: String): List<MedicationScheduleResponse>
    suspend fun getParentSchedules(parentId: Long, date: String): List<MedicationScheduleResponse>
    suspend fun check(logId: Long): MedicationCheckResponse
}

class RemoteMedicationDataSource(private val api: MedicationApi) : MedicationDataSource {
    override suspend fun getMedications(parentId: Long) = api.getMedications(parentId).requireData()
    override suspend fun create(parentId: Long, request: MedicationCreateRequest) =
        api.create(parentId, request).requireData()
    override suspend fun update(parentId: Long, request: MedicationUpdateRequest) =
        api.update(parentId, request).requireData()
    override suspend fun delete(parentId: Long, groupId: String) = api.delete(parentId, groupId).requireData()
    override suspend fun getMySchedules(date: String) = api.getMySchedules(date).requireData()
    override suspend fun getParentSchedules(parentId: Long, date: String) =
        api.getParentSchedules(parentId, date).requireData()
    override suspend fun check(logId: Long) = api.check(logId).requireData()
}
