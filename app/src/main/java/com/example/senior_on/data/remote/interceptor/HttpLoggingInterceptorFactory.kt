package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.BuildConfig
import com.example.senior_on.logging.AppLog
import okhttp3.logging.HttpLoggingInterceptor

object HttpLoggingInterceptorFactory {
    fun create(tag: String): HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
        AppLog.d(tag, debugHttpMessage(message))
    }.apply {
        // Development body inspection only; release never records HTTP traffic.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }
}

internal fun debugHttpMessage(message: String): String {
    if (message.contains("Content-Disposition: form-data", ignoreCase = true)) {
        return "[multipart body omitted]"
    }
    val json = sensitiveJson.replace(message) { "${it.groupValues[1]}\"██\"" }
    return sensitiveQuery.replace(json) { "${it.groupValues[1]}=██" }
}

private val sensitiveJson = Regex(
    """("(?:password|passwordCheck|currentPassword|newPassword|newPasswordCheck|verificationCode|fcmToken|deviceIdentifier|kakaoAccessToken|firebaseIdToken|socialToken|accessToken|refreshToken|uploadUrl|email|name|phoneNumber|birth|address|detailAddress|latitude|longitude)"\s*:\s*)(?:"(?:\\.|[^"\\])*"|-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)""",
    RegexOption.IGNORE_CASE,
)
private val sensitiveQuery = Regex(
    """([?&](?:code|token|accessToken|refreshToken|email|name|phoneNumber|address|latitude|longitude|X-Amz-[\w-]+))=[^&\s]+""",
    RegexOption.IGNORE_CASE,
)

internal fun safeHttpSummary(message: String): String? {
    if (message.startsWith("<-- HTTP FAILED:")) return "HTTP request failed"
    Regex("^--> (GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS) ").find(message)?.let {
        return "HTTP request method=${it.groupValues[1]}"
    }
    Regex("^<-- (\\d{3}) ").find(message)?.let {
        val elapsed = Regex("\\((\\d+)ms(?:[, )])").find(message)?.groupValues?.get(1)
        return "HTTP response status=${it.groupValues[1]}" +
            (elapsed?.let { ms -> " elapsedMs=$ms" } ?: "")
    }
    return null
}
