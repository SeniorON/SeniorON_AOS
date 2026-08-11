package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.source.location.LocationDataSource
import com.example.senior_on.domain.repository.location.LocationRepository

class LocationRepositoryImpl(
    private val source: LocationDataSource,
) : LocationRepository {
    override suspend fun getCurrentLocation() = source.getCurrentLocation()
}
