package com.example.senior_on.location.tracking

import android.content.Context
import com.example.senior_on.data.local.KeystoreTokenCipher

internal enum class OutingTrackingState {
    Home,
    Outing,
}

internal data class StoredHomeLocation(
    val latitude: Double,
    val longitude: Double,
)

internal class OutingTrackingStateStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )
    private val cipher = runCatching { KeystoreTokenCipher() }.getOrNull()

    @Synchronized
    fun saveHomeLocation(latitude: Double, longitude: Double): Boolean {
        val valueCipher = cipher ?: return false
        return runCatching {
            preferences.edit()
                .putString(HomeLatitudeKey, valueCipher.encrypt(latitude.toString()))
                .putString(HomeLongitudeKey, valueCipher.encrypt(longitude.toString()))
                .commit()
        }.getOrDefault(false)
    }

    @Synchronized
    fun getHomeLocation(): StoredHomeLocation? {
        val latitude = readDouble(HomeLatitudeKey) ?: return null
        val longitude = readDouble(HomeLongitudeKey) ?: return null
        return StoredHomeLocation(latitude, longitude)
    }

    @Synchronized
    fun getState(): OutingTrackingState {
        val stored = readValue(StateKey) ?: return OutingTrackingState.Home
        return runCatching { OutingTrackingState.valueOf(stored) }
            .getOrDefault(OutingTrackingState.Home)
    }

    @Synchronized
    fun setState(state: OutingTrackingState): Boolean =
        writeValue(StateKey, state.name)

    fun transition(
        expected: OutingTrackingState,
        next: OutingTrackingState,
    ): Boolean = synchronized(GlobalStateLock) {
        if (getState() != expected) return@synchronized false
        setState(next)
    }

    @Synchronized
    fun clear() {
        preferences.edit().clear().commit()
    }

    private fun readDouble(key: String): Double? = readValue(key)?.toDoubleOrNull()

    private fun readValue(key: String): String? {
        val encrypted = preferences.getString(key, null) ?: return null
        val valueCipher = cipher ?: return null
        return runCatching { valueCipher.decrypt(encrypted) }.getOrNull()
    }

    private fun writeValue(key: String, value: String): Boolean {
        val valueCipher = cipher ?: return false
        return runCatching {
            preferences.edit()
                .putString(key, valueCipher.encrypt(value))
                .commit()
        }.getOrDefault(false)
    }

    private companion object {
        val GlobalStateLock = Any()
        const val PreferencesName = "outing_tracking"
        const val HomeLatitudeKey = "home_latitude"
        const val HomeLongitudeKey = "home_longitude"
        const val StateKey = "tracking_state"
    }
}
