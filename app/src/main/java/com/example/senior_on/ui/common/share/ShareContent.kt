package com.example.senior_on.ui.common.share

data class ShareContent(
    val text: String,
    val androidExecutionParams: Map<String, String> = emptyMap(),
)

sealed interface ShareLaunchResult {
    data object Success : ShareLaunchResult

    data class Failure(
        val userMessage: String,
    ) : ShareLaunchResult
}
