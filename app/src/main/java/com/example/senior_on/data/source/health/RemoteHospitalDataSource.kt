package com.example.senior_on.data.source.health

import com.example.senior_on.data.remote.api.HospitalApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface HospitalDataSource {
    suspend fun getMonthly(parentId: Long, year: Int, month: Int): List<HospitalListResponse>
    suspend fun getDaily(parentId: Long, date: String): List<HospitalDetailResponse>
    suspend fun getUpcoming(parentId: Long): List<HospitalUpcomingResponse>
    suspend fun create(parentId: Long, request: HospitalCreateRequest): HospitalCreateResponse
    suspend fun update(parentId: Long, hospitalId: Long, request: HospitalUpdateRequest)
    suspend fun delete(parentId: Long, hospitalId: Long)
}

class RemoteHospitalDataSource(private val api: HospitalApi) : HospitalDataSource {
    override suspend fun getMonthly(parentId: Long, year: Int, month: Int) =
        api.getMonthly(parentId, year, month).requireData()

    override suspend fun getDaily(parentId: Long, date: String) =
        api.getDaily(parentId, date).requireData()

    override suspend fun getUpcoming(parentId: Long) =
        api.getUpcoming(parentId).requireData()

    override suspend fun create(parentId: Long, request: HospitalCreateRequest) =
        api.create(parentId, request).requireData()

    override suspend fun update(parentId: Long, hospitalId: Long, request: HospitalUpdateRequest) {
        api.update(parentId, hospitalId, request)
    }

    override suspend fun delete(parentId: Long, hospitalId: Long) {
        api.delete(parentId, hospitalId)
    }
}
