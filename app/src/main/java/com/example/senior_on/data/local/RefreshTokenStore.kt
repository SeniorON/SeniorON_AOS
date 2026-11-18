package com.example.senior_on.data.local

interface RefreshTokenStore {
    fun getBearerToken(): String?
    fun getRefreshToken(): String?
    fun getDeviceIdentifier(): String?
    fun saveRefreshedTokens(accessToken: String, newRefreshToken: String)
    fun clear()
}
