package com.example.senior_on.data.source.home

import com.example.senior_on.data.remote.api.HomeApi
import com.example.senior_on.data.remote.dto.*
import com.example.senior_on.data.source.requireData

interface HomeDataSource {
    suspend fun getHome(): HomeResponse
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse
    suspend fun getSeniorHome(): SeniorHomeResponse
    suspend fun getTodayHospitals(): List<TodayHospitalListResponse>
    suspend fun getDevice(): DeviceDetailResponse
    suspend fun getButtonOptions(): List<ButtonOptionResponse>
    suspend fun saveButtons(request: HomeButtonSaveRequest)
    suspend fun addButton(request: HomeButtonCreateRequest): HomeButtonCreateResponse
    suspend fun updateButtons(request: HomeButtonUpdateRequest)
    suspend fun deleteButton(buttonId: Long)
    suspend fun updateFontSize(request: HomeFontSizeUpdateRequest)
    suspend fun updateSeniorProfile(request: SeniorProfileUpdateRequest): SeniorProfileUpdateResponse
}

class RemoteHomeDataSource(private val api: HomeApi) : HomeDataSource {
    override suspend fun getHome() = api.getHome().requireData()
    override suspend fun getWeather(latitude: Double, longitude: Double) =
        api.getWeather(latitude, longitude).requireData()
    override suspend fun getSeniorHome() = api.getSeniorHome().requireData()
    override suspend fun getTodayHospitals() = api.getTodayHospitals().requireData()
    override suspend fun getDevice() = api.getDevice().requireData()
    override suspend fun getButtonOptions() = api.getButtonOptions().requireData()
    override suspend fun saveButtons(request: HomeButtonSaveRequest) { api.saveButtons(request) }
    override suspend fun addButton(request: HomeButtonCreateRequest) = api.addButton(request).requireData()
    override suspend fun updateButtons(request: HomeButtonUpdateRequest) { api.updateButtons(request) }
    override suspend fun deleteButton(buttonId: Long) { api.deleteButton(buttonId) }
    override suspend fun updateFontSize(request: HomeFontSizeUpdateRequest) { api.updateFontSize(request) }
    override suspend fun updateSeniorProfile(request: SeniorProfileUpdateRequest) =
        api.updateSeniorProfile(request).requireData()
}
