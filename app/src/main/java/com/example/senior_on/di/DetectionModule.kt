package com.example.senior_on.di

import com.example.senior_on.device.DetectionConfig

/** Single composition point, also available before Application DI to Workers/DataStore delegates. */
object DetectionModule {
    // Change ONLY this line to DetectionConfig.Qa for detection QA, then rebuild.
    // QaSendFailure is available for retry testing. Release always forces Production.
    private val selected = DetectionConfig.Production
    val config: DetectionConfig = enforceDetectionBuildPolicy(selected)
}
