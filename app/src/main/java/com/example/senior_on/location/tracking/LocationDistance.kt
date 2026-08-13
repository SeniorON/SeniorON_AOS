package com.example.senior_on.location.tracking

import android.location.Location

internal fun distanceMeters(
    firstLatitude: Double,
    firstLongitude: Double,
    secondLatitude: Double,
    secondLongitude: Double,
): Float {
    val result = FloatArray(1)
    Location.distanceBetween(
        firstLatitude,
        firstLongitude,
        secondLatitude,
        secondLongitude,
        result,
    )
    return result[0]
}

internal const val OutingBoundaryMeters = 1_000f
internal const val ReturnBoundaryMeters = 800f
