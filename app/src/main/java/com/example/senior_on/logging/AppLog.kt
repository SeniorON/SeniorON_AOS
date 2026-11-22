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
        if (BuildConfig.DEBUG) Log.w(tag, diagnosticMessage(message, error, true))
    }
    fun e(tag: String, message: String, error: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, diagnosticMessage(message, error, true))
    }
}

// Exception messages and causes can contain server bodies or private URLs.
internal fun diagnosticMessage(message: String, error: Throwable?, debug: Boolean): String =
    if (debug && error != null) "$message; type=${error.javaClass.simpleName}" else message
