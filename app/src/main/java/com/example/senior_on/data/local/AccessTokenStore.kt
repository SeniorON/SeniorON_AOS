package com.example.senior_on.data.local

import android.content.Context
import android.content.SharedPreferences

object AccessTokenStore {
    @Volatile
    private var token: String? = null
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
    }

    fun save(accessToken: String) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
        preferences?.edit()?.putString(ACCESS_TOKEN_KEY, token)?.apply()
    }

    fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    fun clear() {
        token = null
        preferences?.edit()?.remove(ACCESS_TOKEN_KEY)?.apply()
    }

    private const val BEARER_PREFIX = "Bearer "
    private const val TOKEN_PREFERENCES = "auth_token"
    private const val ACCESS_TOKEN_KEY = "access_token"
}
