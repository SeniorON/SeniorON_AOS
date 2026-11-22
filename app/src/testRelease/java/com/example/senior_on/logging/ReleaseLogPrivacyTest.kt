package com.example.senior_on.logging

import com.example.senior_on.BuildConfig
import com.example.senior_on.data.remote.interceptor.withHttpDiagnostics
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseLogPrivacyTest {
    @Test fun releaseInstallsNoHttpLogger() {
        assertFalse(BuildConfig.DEBUG)
        val client = OkHttpClient.Builder().withHttpDiagnostics("test").build()
        assertTrue(client.interceptors.isEmpty())
        assertTrue(client.networkInterceptors.isEmpty())
    }

    @Test fun releaseLogCallsDoNotReachAndroidLog() {
        // Android Log methods are unmocked JVM stubs: invoking them fails this test.
        val cause = IllegalStateException("synthetic private payload")
        AppLog.d("test", "debug")
        AppLog.i("test", "info")
        AppLog.w("test", "warning", cause)
        AppLog.e("test", "error", cause)
    }
}
