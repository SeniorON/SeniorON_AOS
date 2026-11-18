package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.SessionExpirationEventStore
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.TokenRefreshRequest
import com.example.senior_on.data.remote.dto.TokenRefreshResponse
import retrofit2.Response
import java.io.IOException

enum class SessionRecoveryResult { Ready, Expired, Retry }

/** Shared by startup recovery and OkHttp authentication; refresh-token rotation is serialized. */
class SessionTokenRefresher(
    private val tokenStore: com.example.senior_on.data.local.RefreshTokenStore = AccessTokenStore,
    private val request: (TokenRefreshRequest) -> Response<ApiResponse<TokenRefreshResponse>>,
) {
    @Synchronized
    fun refresh(failedAuthorization: String? = null): SessionRecoveryResult {
        val current = tokenStore.getBearerToken()
        if (current != null && current != failedAuthorization) return SessionRecoveryResult.Ready
        val refresh = tokenStore.getRefreshToken() ?: return synchronized(tokenStore) {
            if (tokenStore.getBearerToken() != current || tokenStore.getRefreshToken() != null) {
                SessionRecoveryResult.Retry
            } else {
                tokenStore.clear()
                SessionExpirationEventStore.publish()
                SessionRecoveryResult.Expired
            }
        }
        val device = tokenStore.getDeviceIdentifier()
        val response = try {
            request(TokenRefreshRequest(refresh, device))
        } catch (_: IOException) {
            return SessionRecoveryResult.Retry
        } catch (_: com.google.gson.JsonParseException) {
            return SessionRecoveryResult.Retry
        }

        // A logout or another login while the request was running must not resurrect the old session.
        return synchronized(tokenStore) {
            if (tokenStore.getRefreshToken() != refresh ||
                tokenStore.getDeviceIdentifier() != device ||
                tokenStore.getBearerToken() != current
            ) {
                response.errorBody()?.close()
                return@synchronized if (tokenStore.getBearerToken() != null)
                    SessionRecoveryResult.Ready else SessionRecoveryResult.Retry
            }
            if (!response.isSuccessful) {
                response.errorBody()?.close()
                if (response.code() in setOf(400, 401, 403)) {
                    tokenStore.clear()
                    SessionExpirationEventStore.publish()
                    return@synchronized SessionRecoveryResult.Expired
                }
                return@synchronized SessionRecoveryResult.Retry
            }
            val tokens = response.body()?.data
            val access = tokens?.accessToken?.removePrefix("Bearer ")?.trim()
                ?.takeIf { it.isNotEmpty() } ?: return@synchronized SessionRecoveryResult.Retry
            try {
                tokenStore.saveRefreshedTokens(
                    access,
                    tokens.refreshToken?.trim()?.takeIf { it.isNotEmpty() } ?: refresh,
                )
            } catch (_: IOException) {
                return@synchronized SessionRecoveryResult.Retry
            }
            SessionRecoveryResult.Ready
        }
    }
}
