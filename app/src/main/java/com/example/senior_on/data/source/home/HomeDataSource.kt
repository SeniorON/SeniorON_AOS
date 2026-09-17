package com.example.senior_on.data.source.home

import com.example.senior_on.data.remote.api.HomeApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData
import com.example.senior_on.data.source.remoteRequest

interface HomeDataSource {
    suspend fun getHome(seniorId: Long): HomeResponse
    suspend fun getSeniorHome(): SeniorHomeResponse
    suspend fun getTodayHospitals(seniorId: Long): List<TodayHospitalListResponse>
    suspend fun getDevice(seniorId: Long): DeviceDetailResponse
    suspend fun getButtonOptions(seniorId: Long): List<ButtonOptionResponse>
    suspend fun saveButtons(request: HomeButtonSaveRequest)
    suspend fun updateFontSize(request: HomeFontSizeUpdateRequest)
    suspend fun updateSeniorProfile(
        request: SeniorProfileUpdateRequest,
    ): SeniorProfileUpdateResponse
}

class RemoteHomeDataSource(private val api: HomeApi) : HomeDataSource {
    override suspend fun getHome(seniorId: Long) = remoteRequest {
        api.getHome(seniorId).requireData()
    }
    override suspend fun getSeniorHome() = api.getSeniorHome().requireData()
    override suspend fun getTodayHospitals(seniorId: Long) =
        api.getTodayHospitals(seniorId).requireData()
    override suspend fun getDevice(seniorId: Long) = api.getDevice(seniorId).requireData()
    override suspend fun getButtonOptions(seniorId: Long) =
        api.getButtonOptions(seniorId).requireData()
    override suspend fun saveButtons(request: HomeButtonSaveRequest) {
        api.saveButtons(request)
    }
    override suspend fun updateFontSize(request: HomeFontSizeUpdateRequest) {
        api.updateFontSize(request)
    }
    override suspend fun updateSeniorProfile(
        request: SeniorProfileUpdateRequest,
    ) = api.updateSeniorProfile(request).requireData()
}
