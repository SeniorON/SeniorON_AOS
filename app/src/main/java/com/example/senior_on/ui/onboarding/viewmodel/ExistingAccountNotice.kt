package com.example.senior_on.ui.onboarding.viewmodel

internal enum class ExistingAccountNotice(val title: String, val guidance: String) {
    General("일반 회원가입으로 만든 계정이 있어요", "아이디와 비밀번호로 로그인해 주세요."),
    Social("소셜 로그인으로 가입한 계정이 있어요", "가입할 때 사용한 소셜 계정으로 로그인해 주세요."),
    Unspecified("이미 이 이메일로 가입한 계정이 있어요", "기존 계정으로 로그인해 주세요.");

    companion object {
        fun fromCode(code: String?) = when (code) {
            "USER409_6" -> General
            "USER409_4", "USER409_5" -> Social
            "USER409" -> Unspecified
            else -> null
        }
    }
}
