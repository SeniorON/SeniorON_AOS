package com.example.senior_on.device

import android.content.Context

data class ParentInactivityState(
    val lastActiveAtMillis: Long?,
    val thresholdHours: Int,
    val enabled: Boolean,
    val alertedForActivityAtMillis: Long?,
)

class ParentInactivityStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun recordActivity(atMillis: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong(LastActiveAtKey, atMillis)
            .remove(AlertedForActivityAtKey)
            .apply()
    }

    fun updateSetting(thresholdHours: Int, enabled: Boolean) {
        preferences.edit()
            .putInt(ThresholdHoursKey, thresholdHours.coerceAtLeast(1))
            .putBoolean(EnabledKey, enabled)
            .apply()
    }

    fun snapshot(): ParentInactivityState = ParentInactivityState(
        lastActiveAtMillis = preferences.optionalLong(LastActiveAtKey),
        thresholdHours = preferences.getInt(ThresholdHoursKey, DefaultThresholdHours)
            .coerceAtLeast(1),
        enabled = preferences.getBoolean(EnabledKey, DefaultEnabled),
        alertedForActivityAtMillis = preferences.optionalLong(AlertedForActivityAtKey),
    )

    fun markAlertSentFor(lastActiveAtMillis: Long) {
        preferences.edit()
            .putLong(AlertedForActivityAtKey, lastActiveAtMillis)
            .apply()
    }

    private fun android.content.SharedPreferences.optionalLong(key: String): Long? =
        getLong(key, MissingTimestamp).takeUnless { it == MissingTimestamp }

    private companion object {
        const val PreferencesName = "parent_activity_state"
        const val LastActiveAtKey = "last_active_at"
        const val ThresholdHoursKey = "inactivity_threshold_hours"
        const val EnabledKey = "inactivity_enabled"
        const val AlertedForActivityAtKey = "inactivity_alerted_for_activity_at"
        const val MissingTimestamp = -1L
        const val DefaultThresholdHours = 4
        const val DefaultEnabled = true
    }
}
