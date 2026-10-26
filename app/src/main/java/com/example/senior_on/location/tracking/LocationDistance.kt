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

internal data class OutingBoundaries(val outing: Float, val home: Float)

internal fun outingBoundaries(qa: Boolean): OutingBoundaries =
    (if (qa) com.example.senior_on.device.DetectionConfig.Qa else com.example.senior_on.device.DetectionConfig.Production)
        .let { OutingBoundaries(it.outingMeters, it.returnMeters) }

internal val activeOutingBoundaries: OutingBoundaries
    get() = com.example.senior_on.di.DetectionModule.config.let { OutingBoundaries(it.outingMeters, it.returnMeters) }
