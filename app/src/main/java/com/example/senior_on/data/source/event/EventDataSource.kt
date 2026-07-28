package com.example.senior_on.data.source.event

import com.example.senior_on.data.remote.api.EventApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface EventDataSource {
    suspend fun createSos(request: SosEventRequest): SosEventResponse
    suspend fun createRiskLink(request: RiskLinkRequest): RiskLinkResponse
    suspend fun createOutingReturn(request: OutingReturnRequest): OutingReturnResponse
    suspend fun createInactivity(request: InactivityRequest): InactivityResponse
    suspend fun getDetail(eventId: Long): EventDetailResponse
}

class RemoteEventDataSource(private val api: EventApi) : EventDataSource {
    override suspend fun createSos(request: SosEventRequest) = api.createSos(request).requireData()
    override suspend fun createRiskLink(request: RiskLinkRequest) =
        api.createRiskLink(request).requireData()
    override suspend fun createOutingReturn(request: OutingReturnRequest) =
        api.createOutingReturn(request).requireData()
    override suspend fun createInactivity(request: InactivityRequest) =
        api.createInactivity(request).requireData()
    override suspend fun getDetail(eventId: Long) = api.getDetail(eventId).requireData()
}
