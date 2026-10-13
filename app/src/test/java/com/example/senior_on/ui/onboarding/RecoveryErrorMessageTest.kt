package com.example.senior_on.ui.onboarding

import com.example.senior_on.domain.model.auth.RemoteRequestException
import com.example.senior_on.ui.onboarding.viewmodel.recoveryLookupErrorText
import java.net.UnknownHostException
import org.junit.Assert.*
import org.junit.Test

class RecoveryErrorMessageTest {
    private fun remote(status: Int, code: String) =
        RemoteRequestException(status, code, null, "internal server details", Exception())

    @Test fun accountMismatchUsesFieldsForSelectedFlow() {
        assertTrue(recoveryLookupErrorText(remote(404, "USER404_1"), false).contains("이름과 이메일"))
        assertTrue(recoveryLookupErrorText(remote(404, "USER404_2"), true).contains("이름과 아이디"))
    }

    @Test fun networkAndServerErrorsDoNotBlameAccountInput() {
        assertTrue(recoveryLookupErrorText(UnknownHostException("host"), false).contains("네트워크"))
        val message = recoveryLookupErrorText(remote(500, "COMMON500"), true)
        assertFalse(message.contains("internal"))
        assertFalse(message.contains("계정을 찾지"))
    }

    @Test fun emailAndRateLimitFailuresAreActionable() {
        assertTrue(recoveryLookupErrorText(remote(500, "EMAIL500"), true).contains("인증번호"))
        assertTrue(recoveryLookupErrorText(remote(429, "EMAIL429_1"), true).contains("잠시 후"))
    }
}
