package com.example.senior_on.data.remote.dto

data class HospitalCreateRequest(
    val hospitalName: String,
    val department: String,
    val scheduleDate: String,
    val scheduleTime: String,
    val reminderType: String,
)

data class HospitalUpdateRequest(
    val hospitalName: String,
    val department: String,
    val scheduleDate: String,
    val scheduleTime: String,
    val reminderType: String,
)

data class HospitalCreateResponse(
    val hospitalId: Long?,
    val hospitalName: String?,
    val department: String?,
    val scheduleDate: String?,
    val scheduleTime: String?,
    val reminderType: String?,
)

data class HospitalListResponse(
    val hospitalId: Long?,
    val hospitalName: String?,
    val department: String?,
    val scheduleDate: String?,
    val scheduleTime: String?,
    val reminderType: String?,
)

data class HospitalDetailResponse(
    val hospitalId: Long?,
    val hospitalName: String?,
    val department: String?,
    val scheduleDate: String?,
    val scheduleTime: String?,
    val reminderType: String?,
)

data class HospitalUpcomingResponse(
    val scheduleDate: String? = null,
    val schedules: List<HospitalDetailResponse>? = null,
)
