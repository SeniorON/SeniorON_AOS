package com.example.senior_on.data.local

import android.util.Log
import com.example.senior_on.BuildConfig

object AccessTokenStore {
    @Volatile
    private var token: String? = null

    fun save(accessToken: String) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "AccessToken: ${token.orEmpty()}")
        }
    }

    fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    fun clear() {
        token = null
    }

    private const val BEARER_PREFIX = "Bearer "
    private const val LOG_TAG = "SeniorOnAuth"
}
