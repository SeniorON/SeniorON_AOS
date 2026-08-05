package com.example.senior_on.data.local

import android.content.Context
import android.content.SharedPreferences

object AccessTokenStore {
    @Volatile
    private var token: String? = null
    @Volatile
    private var refreshToken: String? = null
    @Volatile
    private var deviceIdentifier: String? = null
    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            TOKEN_PREFERENCES,
            Context.MODE_PRIVATE,
        )
        token = preferences?.getString(ACCESS_TOKEN_KEY, null)
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        refreshToken = preferences?.getString(REFRESH_TOKEN_KEY, null)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        deviceIdentifier = preferences?.getString(DEVICE_IDENTIFIER_KEY, null)
            ?.trim()
            ?.takeIf(String::isNotEmpty)
    }

    fun save(accessToken: String) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        preferences?.edit()?.putString(ACCESS_TOKEN_KEY, token)?.apply()
    }

    @Synchronized
    fun saveLoginTokens(
        accessToken: String,
        newRefreshToken: String?,
        newDeviceIdentifier: String,
    ) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        refreshToken = newRefreshToken?.trim()?.takeIf(String::isNotEmpty)
        deviceIdentifier = newDeviceIdentifier.trim().takeIf(String::isNotEmpty)

        preferences?.edit()
            ?.putString(ACCESS_TOKEN_KEY, token)
            ?.putString(REFRESH_TOKEN_KEY, refreshToken)
            ?.putString(DEVICE_IDENTIFIER_KEY, deviceIdentifier)
            ?.apply()
    }

    @Synchronized
    fun saveRefreshedTokens(
        accessToken: String,
        newRefreshToken: String,
    ) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        refreshToken = newRefreshToken.trim().takeIf(String::isNotEmpty)
        preferences?.edit()
            ?.putString(ACCESS_TOKEN_KEY, token)
            ?.putString(REFRESH_TOKEN_KEY, refreshToken)
            ?.apply()
    }

    fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    fun getRefreshToken(): String? = refreshToken

    fun getDeviceIdentifier(): String? = deviceIdentifier

    fun clear() {
        token = null
        refreshToken = null
        deviceIdentifier = null
        preferences?.edit()
            ?.remove(ACCESS_TOKEN_KEY)
            ?.remove(REFRESH_TOKEN_KEY)
            ?.remove(DEVICE_IDENTIFIER_KEY)
            ?.apply()
    }

    private const val BEARER_PREFIX = "Bearer "
    private const val TOKEN_PREFERENCES = "auth_token"
    private const val ACCESS_TOKEN_KEY = "access_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val DEVICE_IDENTIFIER_KEY = "device_identifier"
}
