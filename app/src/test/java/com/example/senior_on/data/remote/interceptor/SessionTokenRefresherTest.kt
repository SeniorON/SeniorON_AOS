package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.data.local.RefreshTokenStore
import com.example.senior_on.data.local.SessionExpirationEventStore
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.data.remote.dto.TokenRefreshResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SessionTokenRefresherTest {
    private val tokenStore = FakeTokenStore()
    @Before fun setup() {
        SessionExpirationEventStore.consume()
        tokenStore.set(null, "refresh", "device")
    }

    @After fun cleanup() {
        tokenStore.clear()
        SessionExpirationEventStore.consume()
    }

    private fun success(access: String? = "new-access", refresh: String? = "new-refresh") =
        Response.success(ApiResponse("200 OK", "COMMON_200", "OK", TokenRefreshResponse(access, refresh)))

    @Test fun missingAccessTokenIsRecoveredUsingRefreshAndDevice() {
        val refresher = SessionTokenRefresher(tokenStore) {
            assertEquals("refresh", it.refreshToken)
            assertEquals("device", it.deviceIdentifier)
            success()
        }
        assertEquals(SessionRecoveryResult.Ready, refresher.refresh())
        assertEquals("Bearer new-access", tokenStore.getBearerToken())
        assertEquals("new-refresh", tokenStore.getRefreshToken())
    }

    @Test fun offlineKeepsCredentialsAndManualRetrySucceeds() {
        var offline = true
        val refresher = SessionTokenRefresher(tokenStore) {
            if (offline) throw IOException("offline")
            success()
        }
        assertEquals(SessionRecoveryResult.Retry, refresher.refresh())
        assertEquals("refresh", tokenStore.getRefreshToken())
        assertEquals("device", tokenStore.getDeviceIdentifier())
        assertNull(SessionExpirationEventStore.pendingEvent.value)
        offline = false
        assertEquals(SessionRecoveryResult.Ready, refresher.refresh())
    }

    @Test fun serverFailureKeepsRefreshToken() {
        val refresher = SessionTokenRefresher(tokenStore) { Response.error(503, "unavailable".toResponseBody()) }
        assertEquals(SessionRecoveryResult.Retry, refresher.refresh())
        assertEquals("refresh", tokenStore.getRefreshToken())
        assertNull(SessionExpirationEventStore.pendingEvent.value)
    }

    @Test fun invalidRefreshClearsCredentialsAndPublishesExpiration() {
        val refresher = SessionTokenRefresher(tokenStore) { Response.error(401, "expired".toResponseBody()) }
        assertEquals(SessionRecoveryResult.Expired, refresher.refresh())
        assertNull(tokenStore.getRefreshToken())
        assertNotNull(SessionExpirationEventStore.pendingEvent.value)
    }

    @Test fun malformedSuccessDoesNotEraseSession() {
        val refresher = SessionTokenRefresher(tokenStore) { success(access = null) }
        assertEquals(SessionRecoveryResult.Retry, refresher.refresh())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    @Test fun omittedRefreshKeepsPreviousRefreshToken() {
        assertEquals(SessionRecoveryResult.Ready, SessionTokenRefresher(tokenStore) { success(refresh = null) }.refresh())
        assertEquals("refresh", tokenStore.getRefreshToken())
    }

    @Test fun logoutDuringRequestDoesNotRestoreOldSession() {
        val refresher = SessionTokenRefresher(tokenStore) {
            tokenStore.clear()
            success()
        }
        assertEquals(SessionRecoveryResult.Retry, refresher.refresh())
        assertNull(tokenStore.getBearerToken())
        assertNull(tokenStore.getRefreshToken())
    }

    @Test fun newLoginDuringRequestIsNotOverwritten() {
        val refresher = SessionTokenRefresher(tokenStore) {
            tokenStore.set("other-access", "other-refresh", "other-device")
            success()
        }
        assertEquals(SessionRecoveryResult.Ready, refresher.refresh())
        assertEquals("Bearer other-access", tokenStore.getBearerToken())
        assertEquals("other-refresh", tokenStore.getRefreshToken())
    }

    @Test fun concurrentRecoveryOnlyRotatesOnce() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        var calls = 0
        val refresher = SessionTokenRefresher(tokenStore) {
            calls++
            entered.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            success()
        }
        val pool = Executors.newFixedThreadPool(2)
        try {
            val first = pool.submit<SessionRecoveryResult> { refresher.refresh() }
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            val second = pool.submit<SessionRecoveryResult> { refresher.refresh() }
            release.countDown()
            assertEquals(SessionRecoveryResult.Ready, first.get(5, TimeUnit.SECONDS))
            assertEquals(SessionRecoveryResult.Ready, second.get(5, TimeUnit.SECONDS))
            assertEquals(1, calls)
        } finally {
            release.countDown()
            pool.shutdownNow()
        }
    }

    @Test fun authenticatorPropagatesNetworkFailureInsteadOfReturningOriginal401() {
        tokenStore.set("expired-access", "refresh", "device")
        val authenticator = AccessTokenAuthenticator(SessionTokenRefresher(tokenStore) { throw IOException("offline") })
        val response = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("https://example.test/")
                .header("Authorization", "Bearer expired-access").build())
            .protocol(okhttp3.Protocol.HTTP_1_1).code(401).message("unauthorized").build()
        assertThrows(IOException::class.java) { authenticator.authenticate(null, response) }
        assertEquals("refresh", tokenStore.getRefreshToken())
        assertNull(SessionExpirationEventStore.pendingEvent.value)
    }
}

private class FakeTokenStore : RefreshTokenStore {
    private var access: String? = null
    private var refresh: String? = null
    private var device: String? = null
    fun set(access: String?, refresh: String?, device: String?) {
        this.access = access
        this.refresh = refresh
        this.device = device
    }
    override fun getBearerToken() = access?.let { "Bearer $it" }
    override fun getRefreshToken() = refresh
    override fun getDeviceIdentifier() = device
    override fun saveRefreshedTokens(accessToken: String, newRefreshToken: String) {
        access = accessToken
        refresh = newRefreshToken
    }
    override fun clear() = set(null, null, null)
}
