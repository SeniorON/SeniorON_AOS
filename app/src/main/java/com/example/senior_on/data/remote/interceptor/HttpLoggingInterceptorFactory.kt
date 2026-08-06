package com.example.senior_on.data.remote.interceptor

import android.util.Log
import com.example.senior_on.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

object HttpLoggingInterceptorFactory {
    fun create(tag: String): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.d(tag, message.redactSensitiveValues())
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

    private fun String.redactSensitiveValues(): String {
        val redactedJson = SensitiveJsonValueRegex.replace(this) { match ->
            "${match.groupValues[1]}\"$RedactedValue\""
        }
        return SensitiveQueryValueRegex.replace(redactedJson) { match ->
            "${match.groupValues[1]}=$RedactedValue"
        }
    }

    private val SensitiveJsonValueRegex = Regex(
        pattern = """("(?:password|passwordCheck|currentPassword|newPassword|newPasswordCheck|verificationCode|fcmToken|deviceIdentifier|kakaoAccessToken|firebaseIdToken|accessToken|refreshToken)"\s*:\s*)"[^"]*"""",
        option = RegexOption.IGNORE_CASE,
    )
    private val SensitiveQueryValueRegex = Regex(
        pattern = """([?&](?:code|token|accessToken|refreshToken))=[^&\s]+""",
        option = RegexOption.IGNORE_CASE,
    )
    private const val RedactedValue = "██"
}
