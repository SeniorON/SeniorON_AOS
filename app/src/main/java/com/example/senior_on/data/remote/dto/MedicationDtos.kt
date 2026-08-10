package com.example.senior_on.data.remote.dto

data class MedicationCreateRequest(
    val medicineName: String,
    val ingredientName: String?,
    val medicineTimes: List<String>,
    val startDate: String,
    val repeatType: String,
    val repeatInterval: Int,
    val medicineDays: List<String>,
    val repeatEndType: String,
    val durationWeeks: Int? = null,
    val endDate: String? = null,
)

data class MedicationUpdateRequest(
    val medicationGroupId: String,
    val medicineName: String,
    val ingredientName: String?,
    val medicineTimes: List<String>,
    val startDate: String,
    val repeatType: String,
    val repeatInterval: Int,
    val medicineDays: List<String>,
    val repeatEndType: String,
    val durationWeeks: Int? = null,
    val endDate: String? = null,
)

data class MedicationCreateResponse(
    val medicationIds: List<Long>? = null,
    val medicationGroupId: String? = null,
    val medicineName: String? = null,
    val ingredientName: String? = null,
    val medicineTimes: List<String>? = null,
    val startDate: String? = null,
    val repeatType: String? = null,
    val repeatInterval: Int? = null,
    val medicineDays: List<String>? = null,
    val repeatEndType: String? = null,
    val durationWeeks: Int? = null,
    val endDate: String? = null,
)

data class MedicationReadResponse(
    val medicationId: Long? = null,
    val medicationIds: List<Long>? = null,
    val medicationGroupId: String? = null,
    val medicineName: String? = null,
    val ingredientName: String? = null,
    val medicineTime: String? = null,
    val medicineTimes: List<String>? = null,
    val startDate: String? = null,
    val repeatType: String? = null,
    val repeatInterval: Int? = null,
    val medicineDays: String? = null,
    val medicineDayList: List<String>? = null,
    val repeatEndType: String? = null,
    val durationWeeks: Int? = null,
    val endDate: String? = null,
)

data class MedicationScheduleResponse(
    val medicationLogId: Long?,
    val medicineName: String?,
    val ingredientName: String?,
    val plannedDate: String?,
    val plannedTime: String?,
    val isTaken: Boolean?,
    val status: String?,
)

data class MedicationMonthlyScheduleResponse(
    val year: Int?,
    val month: Int?,
    val scheduledDates: List<String>?,
)

data class MedicationCheckResponse(
    val medicationLogId: Long?,
    val isTaken: Boolean?,
    val takenAt: String?,
)
