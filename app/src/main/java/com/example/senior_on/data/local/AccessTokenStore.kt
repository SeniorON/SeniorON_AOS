package com.example.senior_on.data.local

object AccessTokenStore {
    @Volatile
    private var token: String? = null

    fun save(accessToken: String) {
        token = accessToken.removePrefix(BEARER_PREFIX).trim().takeIf(String::isNotEmpty)
    }

    fun getBearerToken(): String? = token?.let { "$BEARER_PREFIX$it" }

    fun clear() {
        token = null
    }

    private const val BEARER_PREFIX = "Bearer "
}
