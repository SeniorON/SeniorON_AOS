package com.example.senior_on.ui.onboarding

import com.example.senior_on.ui.onboarding.viewmodel.signupVerificationErrorText
import org.junit.Assert.assertEquals
import org.junit.Test

class SignupVerificationErrorTextTest {
    @Test fun alreadyVerifiedGuidesToResendInsteadOfTreatingItAsSuccess() {
        assertEquals("이미 인증된 이메일이에요. 재전송을 눌러 새 인증번호를 받은 뒤 다시 인증해 주세요.",
            signupVerificationErrorText("USER409_2", "이미 인증이 완료된 이메일입니다."))
    }

    @Test fun unrelatedErrorsKeepTheirMessage() {
        assertEquals("번호가 틀렸어요", signupVerificationErrorText("USER400_11", "번호가 틀렸어요"))
        assertEquals("네트워크 오류", signupVerificationErrorText(null, "네트워크 오류"))
    }
}
