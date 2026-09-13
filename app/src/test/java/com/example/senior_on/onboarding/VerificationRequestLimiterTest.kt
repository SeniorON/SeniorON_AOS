package com.example.senior_on.onboarding

import com.example.senior_on.domain.model.auth.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class VerificationRequestLimiterTest {
    private var time = 1_000L
    private val gate = VerificationRequestLimiter { time }
    private val key = gate.emailKey("USER@example.com")
    private fun limited(code: String, retry: Long? = 42) = RemoteRequestException(429, code, retry, "제한", Exception())

    @Test fun successLocksForSixtySecondsAndExpiresAtFiveMinutes() = runTest {
        gate.send(key, { it }) { true }
        assertEquals(61_000L, gate.state(key).blockedUntil)
        assertEquals(301_000L, gate.state(key).expiresAt)
        assertTrue(runCatching { gate.send(key, { it }) { true } }.isFailure)
        time += 60_000
        gate.send(key, { it }) { true }
        assertEquals(361_000L, gate.state(key).expiresAt)
    }

    @Test fun emailLimitsUseServerDelayAndDoNotBlockOtherEmails() = runTest {
        for (code in listOf("EMAIL429_1", "EMAIL429_2", "EMAIL429_3")) {
            val isolated = VerificationRequestLimiter { time }
            runCatching { isolated.send(key, { it: Boolean -> it }) { throw limited(code) } }
            assertEquals(time + 42_000, isolated.state(key).blockedUntil)
            assertEquals(0L, isolated.state("another").blockedUntil)
        }
        assertEquals(key, gate.emailKey(" user@EXAMPLE.com "))
    }

    @Test fun ipLimitBlocksBothFlows() = runTest {
        runCatching { gate.send(key, { it: Boolean -> it }) { throw limited("EMAIL429_4") } }
        assertEquals(time + 42_000, gate.state(gate.recoveryKey("user")).blockedUntil)
    }

    @Test fun failedResendPreservesExistingCodeAndDoesNotLockOnNetworkFailure() = runTest {
        gate.send(key, { it }) { true }
        time += 60_000
        val expiry = gate.state(key).expiresAt
        runCatching { gate.send(key, { it: Boolean -> it }) { throw IllegalStateException("offline") } }
        assertEquals(expiry, gate.state(key).expiresAt)
        assertFalse(gate.state(key).sending)
        assertTrue(gate.state(key).blockedUntil <= time)
    }

    @Test fun pendingRequestCannotBeDuplicated() = runTest {
        val started = CompletableDeferred<Unit>()
        val complete = CompletableDeferred<Boolean>()
        val first = async { gate.send(key, { it }) { started.complete(Unit); complete.await() } }
        started.await()
        assertTrue(runCatching { gate.send(key, { it }) { true } }.isFailure)
        complete.complete(true)
        first.await()
        assertFalse(gate.state(key).sending)
    }

    @Test fun missingHeaderUsesConservativeFallback() = runTest {
        runCatching { gate.send(key, { it: Boolean -> it }) { throw limited("EMAIL429_3", null) } }
        assertEquals(time + 86_400_000, gate.state(key).blockedUntil)
        assertTrue(gate.state(key).reason!!.contains("추정치"))
    }
}
