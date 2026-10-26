package com.example.senior_on.data.remote.interceptor

import com.example.senior_on.BuildConfig
import com.example.senior_on.logging.AppLog
import okhttp3.logging.HttpLoggingInterceptor

object HttpLoggingInterceptorFactory {
    fun create(tag: String): HttpLoggingInterceptor = HttpLoggingInterceptor { message ->
        safeHttpSummary(message)?.let { AppLog.d(tag, it) }
    }.apply {
        // No bodies, headers, filenames or raw URLs, even in debug.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
    }
}

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
