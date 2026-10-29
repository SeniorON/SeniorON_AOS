package com.example.senior_on.ui.onboarding.viewmodel

internal fun signupVerificationErrorText(code: String?, fallback: String): String = when (code) {
    "USER409_2" -> "이미 인증된 이메일이에요. 재전송을 눌러 새 인증번호를 받은 뒤 다시 인증해 주세요."
    else -> fallback
}
