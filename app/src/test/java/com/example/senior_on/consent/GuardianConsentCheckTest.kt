package com.example.senior_on.consent

import com.example.senior_on.ui.consent.GuardianConsentCheck
import com.example.senior_on.data.consent.GuardianAccess
import com.example.senior_on.ui.consent.GuardianConsentPhase.*
import org.junit.Assert.assertEquals
import org.junit.Test

class GuardianConsentCheckTest {
    @Test fun failureAndRetryKeepDraftButNeverShowPreviousApproval() {
        val gate = GuardianConsentCheck()
        assertEquals(Checking, gate.phase)
        gate.start()
        val first = gate.request!!
        gate.finish(first, GuardianAccess.Allowed)
        gate.pause()
        assertEquals(Checking, gate.phase)
        gate.finish(first, GuardianAccess.Allowed)
        assertEquals(Checking, gate.phase)
        gate.start()
        gate.finish(first, GuardianAccess.Allowed)
        assertEquals(Checking, gate.phase)
        gate.finish(gate.request!!, null)
        assertEquals(Failed, gate.phase)
        gate.start()
        assertEquals(Checking, gate.phase)
        gate.finish(gate.request!!, GuardianAccess.Allowed)
        assertEquals(Allowed, gate.phase)
        assertEquals(0, gate.draftGeneration)
    }

    @Test fun revocationRetiresTheOldDraftEvenIfLaterApproved() {
        val gate = GuardianConsentCheck()
        gate.start()
        gate.finish(gate.request!!, GuardianAccess.Denied)
        assertEquals(Denied, gate.phase)
        assertEquals(1, gate.draftGeneration)
        gate.pause()
        gate.start()
        gate.finish(gate.request!!, GuardianAccess.Allowed)
        assertEquals(Allowed, gate.phase)
        assertEquals(1, gate.draftGeneration)
        gate.start()
        gate.finish(gate.request!!, GuardianAccess.NotConnected)
        assertEquals(NotConnected, gate.phase)
        assertEquals(2, gate.draftGeneration)
    }
}
