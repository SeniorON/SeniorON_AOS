package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.local.SessionExpirationEventStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
    private val tokenRefresher: SessionTokenRefresher,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val failedAuthorization = response.request.header(AUTHORIZATION_HEADER) ?: return null

        if (responseCount(response) >= MAX_AUTH_ATTEMPTS) {
            expireSession()
            return null
        }

        return when (tokenRefresher.refresh(failedAuthorization)) {
            SessionRecoveryResult.Ready -> AccessTokenStore.getBearerToken()?.let {
                response.request.withAuthorization(it)
            }
            SessionRecoveryResult.Expired -> null // The refresher already invalidated this session atomically.
            // Do not leak the original 401 to callers that interpret it as a permanent logout.
            SessionRecoveryResult.Retry -> throw java.io.IOException("인증 서버에 연결하지 못했어요. 다시 시도해 주세요.")
        }
    }

    private fun expireSession(): Request? {
        AccessTokenStore.clear()
        SessionExpirationEventStore.publish()
        return null
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
        const val MAX_AUTH_ATTEMPTS = 2
    }
}
