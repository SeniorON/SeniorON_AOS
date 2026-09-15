package com.example.senior_on.onboarding

import com.example.senior_on.data.source.remoteRequest
import com.example.senior_on.domain.model.auth.RemoteRequestException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException

class VerificationRemoteResponseTest {
    private fun response(body: String, retry: String? = "42"): HttpException {
        val raw = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://example.test/verify").build())
            .protocol(Protocol.HTTP_1_1).code(429).message("Too Many Requests")
            .apply { if (retry != null) header("Retry-After", retry) }.build()
        return HttpException(retrofit2.Response.error<Unit>(body.toResponseBody("application/json".toMediaType()), raw))
    }

    @Test fun preservesStatusCodeMessageAndRetryHeader() = runTest {
        val source = response("""{"code":"EMAIL429_2","message":"발송 횟수 초과"}""")
        val error = runCatching { remoteRequest<Unit> { throw source } }.exceptionOrNull() as RemoteRequestException
        assertEquals(429, error.status)
        assertEquals("EMAIL429_2", error.code)
        assertEquals(42L, error.retryAfterSeconds)
        assertEquals("발송 횟수 초과", error.message)
        assertSame(source, error.cause)
    }

    @Test fun oldServerWithoutHeaderStillPreservesCode() = runTest {
        val error = runCatching {
            remoteRequest<Unit> { throw response("""{"code":"EMAIL429_1","message":"wait"}""", null) }
        }.exceptionOrNull() as RemoteRequestException
        assertEquals("EMAIL429_1", error.code)
        assertNull(error.retryAfterSeconds)
    }

    @Test fun malformedBodyDoesNotHideHttpFailure() = runTest {
        val error = runCatching { remoteRequest<Unit> { throw response("not-json", "invalid") } }
            .exceptionOrNull() as RemoteRequestException
        assertEquals(429, error.status)
        assertNull(error.code)
        assertNull(error.retryAfterSeconds)
    }
}
