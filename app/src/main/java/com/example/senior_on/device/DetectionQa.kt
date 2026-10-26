package com.example.senior_on.device

import com.example.senior_on.di.DetectionModule
import com.example.senior_on.logging.AppLog

/** Compatibility facade for diagnostics and early-created storage; selection lives in DI. */
object DetectionQa {
    val enabled: Boolean get() = DetectionModule.config.qaEnabled
    fun inactivityThresholdMillis(serverHours: Int): Long = DetectionModule.config.inactivityThresholdMillis(serverHours)
    fun foregroundIntervalMillis(): Long = DetectionModule.config.foregroundIntervalMillis
    fun storeName(production: String): String = DetectionModule.config.storeName(production)
    fun trace(message: String) {
        if (enabled) AppLog.d("SeniorOnDetectionQA", message)
    }
}
