package com.example.senior_on.data.remote.dto

data class MedicationCreateRequest(
    val medicineName: String, val ingredientName: String?,
    val medicineTimes: List<String>, val medicineDays: List<String>
)
data class MedicationUpdateRequest(
    val medicationGroupId: String, val medicineName: String,
    val ingredientName: String?, val medicineDays: List<String>,
    val medicineTimes: List<String>
)
data class MedicationCreateResponse(
    val medicationIds: List<Long>?, val medicationGroupId: String?,
    val medicineName: String?, val ingredientName: String?,
    val medicineTimes: List<String>?, val medicineDays: List<String>?
)
data class MedicationReadResponse(
    val medicationId: Long?, val medicationGroupId: String?, val medicineName: String?,
    val ingredientName: String?, val medicineTime: String?, val medicineDays: String?
)
data class MedicationScheduleResponse(
    val medicationLogId: Long?, val medicineName: String?,
    val ingredientName: String?, val plannedDate: String?,
    val plannedTime: String?, val isTaken: Boolean?, val status: String?
)
data class MedicationMonthlyScheduleResponse(
    val year: Int?, val month: Int?, val scheduledDates: List<String>?
)
data class MedicationCheckResponse(
    val medicationLogId: Long?, val isTaken: Boolean?, val takenAt: String?
)
