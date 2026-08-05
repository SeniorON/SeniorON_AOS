package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.api.UserApi
import com.example.senior_on.data.remote.dto.TokenRefreshRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    private val userApi: UserApi,
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_AUTH_ATTEMPTS) return null

        val failedAuthorization = response.request.header(AUTHORIZATION_HEADER) ?: return null

        return synchronized(refreshLock) {
            val currentAuthorization = AccessTokenStore.getBearerToken() ?: return@synchronized null

            if (failedAuthorization != currentAuthorization) {
                return@synchronized response.request.withAuthorization(currentAuthorization)
            }

            val refreshToken = AccessTokenStore.getRefreshToken() ?: return@synchronized null
            val refreshResponse = runCatching {
                userApi.refreshToken(
                    TokenRefreshRequest(
                        refreshToken = refreshToken,
                        deviceIdentifier = AccessTokenStore.getDeviceIdentifier(),
                    )
                ).execute()
            }.getOrNull() ?: return@synchronized null

            if (!refreshResponse.isSuccessful) {
                if (refreshResponse.code() in INVALID_REFRESH_TOKEN_STATUS_CODES) {
                    AccessTokenStore.clear()
                }
                refreshResponse.errorBody()?.close()
                return@synchronized null
            }

            val refreshedTokens = refreshResponse.body()?.data ?: return@synchronized null
            val newAccessToken = refreshedTokens.accessToken
                ?.removePrefix(BEARER_PREFIX)
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: return@synchronized null
            val newRefreshToken = refreshedTokens.refreshToken
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: refreshToken

            AccessTokenStore.saveRefreshedTokens(
                accessToken = newAccessToken,
                newRefreshToken = newRefreshToken,
            )
            response.request.withAuthorization("$BEARER_PREFIX$newAccessToken")
        }
    }

    private fun Request.withAuthorization(token: String): Request =
        newBuilder()
            .header(AUTHORIZATION_HEADER, token)
            .build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val MAX_AUTH_ATTEMPTS = 2
        val INVALID_REFRESH_TOKEN_STATUS_CODES = setOf(400, 401, 403)
    }
}
