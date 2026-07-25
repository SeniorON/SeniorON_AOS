package com.example.senior_on.domain.model

enum class ParentLinkSafetyVerdict {
    Safe,
    Dangerous
}

data class ParentLinkSafetyResult(
    val url: String,
    val verdict: ParentLinkSafetyVerdict
)
