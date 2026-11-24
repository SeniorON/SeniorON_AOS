package com.example.senior_on.consent

import com.example.senior_on.data.consent.RemoteHealthConsents
import com.example.senior_on.data.consent.GuardianAccess
import com.example.senior_on.data.remote.api.GuardianConsentPayload
import com.example.senior_on.data.remote.api.ConsentPayload
import com.example.senior_on.data.remote.api.HealthConsentApi
import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.ui.onboarding.signup.ConsentTerm
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HealthConsentTest {
    @Test fun rolesHaveDifferentSignupRequirements() {
        val guardian = ConsentTerm.forRole(false)
        val senior = ConsentTerm.forRole(true)
        assertEquals(5, guardian.size)
        assertEquals(7, senior.size)
        assertTrue(senior.take(5).all { it.required })
        assertEquals(listOf(ConsentTerm.Health, ConsentTerm.Marketing), senior.drop(5))
        assertTrue(guardian.take(4).all { it.required })
        assertEquals(listOf(ConsentTerm.Marketing), guardian.drop(4))
        assertFalse(ConsentTerm.Health in guardian)
        assertFalse(ConsentTerm.Location in guardian)
        assertFalse(ConsentTerm.Health.required)
        assertTrue(ConsentTerm.Location.required)
        assertTrue(ConsentTerm.Family in guardian)
    }

    @Test fun selectedSeniorConsentIsIndependentAndRevocable() = runBlocking {
        fun response(value: Boolean?) = ApiResponse("200 OK", "COMMON_200", "", ConsentPayload(value))
        val calls = mutableListOf<String>()
        val repository = RemoteHealthConsents(object : HealthConsentApi {
            override suspend fun mine() = response(false).also { calls += "me" }
            override suspend fun forSenior(seniorId: Long) =
                ApiResponse("200 OK", "COMMON_200", "", when (seniorId) {
                    10L -> GuardianConsentPayload("CONNECTED", true)
                    11L -> GuardianConsentPayload("CONNECTED", false)
                    12L -> GuardianConsentPayload("CONNECTED", null)
                    13L -> GuardianConsentPayload("NOT_CONNECTED", null)
                    else -> GuardianConsentPayload(null, true)
                }).also { calls += "senior/$seniorId" }
            override suspend fun updateMine(body: ConsentPayload) =
                response(false).also { calls += "patch/${body.medicationConsent}" }
        })
        assertFalse(repository.mine())
        assertEquals(GuardianAccess.Allowed, repository.forSenior(10))
        assertEquals(GuardianAccess.Denied, repository.forSenior(11))
        assertEquals(GuardianAccess.Denied, repository.forSenior(12))
        assertEquals(GuardianAccess.NotConnected, repository.forSenior(13))
        assertTrue(runCatching { repository.forSenior(99) }.isFailure)
        // A PATCH response is authoritative, even if it differs from the requested value.
        assertFalse(repository.updateMine(true))
        assertEquals(listOf("me", "senior/10", "senior/11", "senior/12", "senior/13", "senior/99", "patch/true"), calls)
    }
}
