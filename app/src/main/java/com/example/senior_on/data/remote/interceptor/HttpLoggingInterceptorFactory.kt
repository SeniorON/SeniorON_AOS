package com.example.senior_on.data.remote.interceptor

import android.util.Log
import com.example.senior_on.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

object HttpLoggingInterceptorFactory {
    fun create(tag: String): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(tag, message.toSafeLogMessage().redactSensitiveValues())
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }
    }

    private fun String.toSafeLogMessage(): String {
        if (!contains("Content-Disposition: form-data", ignoreCase = true)) return this

        val contentType = MultipartContentTypeRegex.find(this)?.groupValues?.get(1)
        val isFileBody = contentType?.let { type ->
            type.startsWith("audio/", ignoreCase = true) ||
                type.startsWith("image/", ignoreCase = true) ||
                type.equals("application/octet-stream", ignoreCase = true)
        } == true
        if (!isFileBody) return this

        val fileName = MultipartFileNameRegex.find(this)?.groupValues?.get(1)
        return buildString {
            append("[multipart file body omitted")
            fileName?.let { append(", filename=").append(it) }
            contentType?.let { append(", contentType=").append(it) }
            append(']')
        }
    }

    private fun String.redactSensitiveValues(): String {
        val redactedJson = SensitiveJsonValueRegex.replace(this) { match ->
            "${match.groupValues[1]}\"$RedactedValue\""
        }
        return SensitiveQueryValueRegex.replace(redactedJson) { match ->
            "${match.groupValues[1]}=$RedactedValue"
        }
    }

    private val SensitiveJsonValueRegex = Regex(
        pattern = """("(?:password|passwordCheck|currentPassword|newPassword|newPasswordCheck|verificationCode|fcmToken|deviceIdentifier|kakaoAccessToken|firebaseIdToken|socialToken|accessToken|refreshToken|uploadUrl)"\s*:\s*)"[^"]*"""",
        option = RegexOption.IGNORE_CASE,
    )
    private val SensitiveQueryValueRegex = Regex(
        pattern = """([?&](?:code|token|accessToken|refreshToken|X-Amz-Algorithm|X-Amz-Credential|X-Amz-Date|X-Amz-Expires|X-Amz-SignedHeaders|X-Amz-Signature|X-Amz-Security-Token))=[^&\s]+""",
        option = RegexOption.IGNORE_CASE,
    )
    private val MultipartFileNameRegex = Regex(
        pattern = """filename="([^"]+)""",
        option = RegexOption.IGNORE_CASE,
    )
    private val MultipartContentTypeRegex = Regex(
        pattern = """Content-Type:\s*([^\r\n]+)""",
        option = RegexOption.IGNORE_CASE,
    )
    private const val RedactedValue = "██"
}
