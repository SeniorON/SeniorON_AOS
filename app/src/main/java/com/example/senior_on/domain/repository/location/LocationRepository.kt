package com.example.senior_on.domain.repository.location

import com.example.senior_on.domain.model.location.GeoLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): GeoLocation
}
