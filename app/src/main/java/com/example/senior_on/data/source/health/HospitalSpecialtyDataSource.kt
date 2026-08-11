package com.example.senior_on.data.source.health

interface HospitalSpecialtyDataSource {
    suspend fun getSpecialties(): List<String>
}
