package com.example.senior_on.map

import android.os.Build

object KakaoMapAvailability {
    private val supportedAbis = setOf("arm64-v8a", "armeabi-v7a")

    fun isSupportedDevice(): Boolean =
        Build.SUPPORTED_ABIS.any(supportedAbis::contains)
}
