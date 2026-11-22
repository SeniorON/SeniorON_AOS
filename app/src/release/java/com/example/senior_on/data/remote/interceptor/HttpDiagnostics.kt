package com.example.senior_on.data.remote.interceptor

import okhttp3.OkHttpClient

// Never attach a traffic logger to production requests, including token refreshes.
@Suppress("UNUSED_PARAMETER")
internal fun OkHttpClient.Builder.withHttpDiagnostics(tag: String): OkHttpClient.Builder = this
