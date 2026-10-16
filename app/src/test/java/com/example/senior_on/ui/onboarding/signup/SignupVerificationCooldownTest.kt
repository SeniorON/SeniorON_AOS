package com.example.senior_on.ui.onboarding.signup

import org.junit.Assert.assertEquals
import org.junit.Test

class SignupVerificationCooldownTest {
    @Test fun countdownUsesMinutesAndSeconds() {
        assertEquals("잠시 후 다시 요청해 주세요. (00:42)", signupVerificationCooldownText(42))
        assertEquals("잠시 후 다시 요청해 주세요. (01:00)", signupVerificationCooldownText(60))
        assertEquals("잠시 후 다시 요청해 주세요. (00:00)", signupVerificationCooldownText(0))
        assertEquals("잠시 후 다시 요청해 주세요. (00:00)", signupVerificationCooldownText(-1))
    }
}
