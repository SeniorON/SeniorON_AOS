package com.example.senior_on.logging

import android.util.Log
import com.example.senior_on.BuildConfig

/** Only pass reviewed diagnostics, never personal data, payloads or URLs. */
object AppLog {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }
    fun i(tag: String, message: String) = d(tag, message)
    fun w(tag: String, message: String, error: Throwable? = null) {
        Log.w(tag, diagnosticMessage(message, error, BuildConfig.DEBUG))
    }
    fun e(tag: String, message: String, error: Throwable? = null) {
        Log.e(tag, diagnosticMessage(message, error, BuildConfig.DEBUG))
    }
}

// Exception messages and causes can contain server bodies or private URLs.
internal fun diagnosticMessage(message: String, error: Throwable?, debug: Boolean): String =
    if (debug && error != null) "$message; type=${error.javaClass.simpleName}" else message
