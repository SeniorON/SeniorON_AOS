package com.example.senior_on.device

internal fun restrictDetectionConfig(selected: DetectionConfig, allowQa: Boolean): DetectionConfig =
    if (allowQa) selected else DetectionConfig.Production

/** Immutable presets keep timing, delivery, UI and storage selection consistent. */
enum class DetectionConfig(
    val qaEnabled: Boolean,
    val foregroundIntervalMillis: Long,
    val outingMeters: Float,
    val returnMeters: Float,
    val simulateSendFailure: Boolean = false,
) {
    Production(false, 300_000L, 1_000f, 800f),
    Qa(true, 30_000L, 150f, 100f),
    QaSendFailure(true, 30_000L, 150f, 100f, true);

    fun inactivityThresholdMillis(serverHours: Int): Long =
        if (qaEnabled) 120_000L else java.util.concurrent.TimeUnit.HOURS.toMillis(serverHours.toLong())

    fun storeName(production: String): String = if (qaEnabled) "${production}_qa" else production
}
