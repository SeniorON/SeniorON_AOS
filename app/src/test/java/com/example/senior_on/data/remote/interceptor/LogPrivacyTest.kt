package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.logging.diagnosticMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogPrivacyTest {
    @Test fun debugBodyKeepsFieldsButMasksKnownSensitiveValues() {
        assertEquals(
            """{"enabled":true,"count":2,"accessToken":"██","name":"██","latitude":"██"}""",
            debugHttpMessage("""{"enabled":true,"count":2,"accessToken":"secret","name":"private","latitude":37.5}"""),
        )
    }

    @Test fun debugMasksEscapedPasswordsAndSignedQueryValues() {
        assertEquals("""{"password":"██"}""", debugHttpMessage("""{"password":"a\"b"}"""))
        assertEquals("https://example.com?X-Amz-Signature=██&page=1",
            debugHttpMessage("https://example.com?X-Amz-Signature=secret&page=1"))
        assertEquals("[multipart body omitted]",
            debugHttpMessage("Content-Disposition: form-data; filename=private.jpg"))
    }

    @Test fun requestOmitsUrlIdentifiersAndQuery() {
        assertEquals("HTTP request method=POST", safeHttpSummary(
            "--> POST https://example.com/users/123?email=private@example.com&token=secret"))
    }

    @Test fun responseKeepsOnlyStatusAndElapsedTime() {
        assertEquals("HTTP response status=200 elapsedMs=42", safeHttpSummary(
            "<-- 200 https://example.com/photo/private (42ms, 100-byte body)"))
    }

    @Test fun bodiesHeadersAndFileMetadataAreOmitted() {
        listOf("{\"latitude\":37.5,\"name\":\"private\"}",
            "Authorization: Bearer secret", "Content-Disposition: filename=private.jpg",
            "email=private@example.com").forEach { assertNull(safeHttpSummary(it)) }
    }

    @Test fun networkFailureOmitsExceptionText() {
        assertEquals("HTTP request failed", safeHttpSummary(
            "<-- HTTP FAILED: IOException: https://example.com?token=secret"))
    }

    @Test fun exceptionDetailsNeverIncludeMessageOrCause() {
        val error = IllegalStateException("private address", RuntimeException("secret token"))
        assertEquals("REQUEST_FAILED; type=IllegalStateException", diagnosticMessage("REQUEST_FAILED", error, true))
        assertEquals("REQUEST_FAILED", diagnosticMessage("REQUEST_FAILED", error, false))
    }
}
