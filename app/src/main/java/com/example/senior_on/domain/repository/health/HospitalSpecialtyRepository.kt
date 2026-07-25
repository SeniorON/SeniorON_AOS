package com.example.senior_on.domain.repository.health

interface HospitalSpecialtyRepository {
    suspend fun getSpecialties(): List<String>
}
