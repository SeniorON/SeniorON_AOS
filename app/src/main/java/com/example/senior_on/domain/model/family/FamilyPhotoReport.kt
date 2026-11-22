package com.example.senior_on.domain.model.family

sealed interface FamilyPhotoReportResult {
    data object Created : FamilyPhotoReportResult
    data class Duplicate(val message: String, val hidden: Boolean) : FamilyPhotoReportResult
}

enum class FamilyPhotoReportReason {
    Advertisement,
    Copyright,
    Sexual,
    Abuse,
    Privacy,
    Duplicate,
    Etc,
}
