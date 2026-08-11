package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.health.HospitalSpecialtyDataSource
import com.example.senior_on.domain.repository.health.HospitalSpecialtyRepository

class HospitalSpecialtyRepositoryImpl(
    private val dataSource: HospitalSpecialtyDataSource
) : HospitalSpecialtyRepository {
    override suspend fun getSpecialties(): List<String> =
        dataSource.getSpecialties()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
}
