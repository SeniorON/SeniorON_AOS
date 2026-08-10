package com.example.senior_on.ui.child.notification.route

import android.content.Context
import android.content.Intent
import android.net.Uri

internal fun openPhoneDialer(
    context: Context,
    phoneNumber: String?,
): Boolean {
    val normalizedNumber = phoneNumber
        ?.filter { character -> character.isDigit() || character == '+' }
        ?.takeIf(String::isNotBlank)
        ?: return false
    val intent = Intent(
        Intent.ACTION_DIAL,
        Uri.fromParts("tel", normalizedNumber, null),
    )
    return runCatching { context.startActivity(intent) }.isSuccess
}

internal fun openKakaoMapDirections(
    context: Context,
    latitude: Double,
    longitude: Double,
    destinationName: String,
    startLatitude: Double? = null,
    startLongitude: Double? = null,
): Boolean {
    val encodedDestinationName = Uri.encode(destinationName)
    val directionsUri = Uri.parse(
        "https://map.kakao.com/link/to/" +
            "$encodedDestinationName,$latitude,$longitude",
    )
    val routeUriBuilder = Uri.Builder()
        .scheme("kakaomap")
        .authority("route")
    if (startLatitude != null && startLongitude != null) {
        routeUriBuilder.appendQueryParameter(
            "sp",
            "$startLatitude,$startLongitude",
        )
    }
    val kakaoMapIntent = Intent(
        Intent.ACTION_VIEW,
        routeUriBuilder
            .appendQueryParameter("ep", "$latitude,$longitude")
            .appendQueryParameter("by", "car")
            .build(),
    ).apply {
        setPackage(KAKAO_MAP_PACKAGE_NAME)
    }
    if (runCatching { context.startActivity(kakaoMapIntent) }.isSuccess) {
        return true
    }

    val webDirectionsIntent = Intent(
        Intent.ACTION_VIEW,
        directionsUri,
    )
    return runCatching { context.startActivity(webDirectionsIntent) }.isSuccess
}

private const val KAKAO_MAP_PACKAGE_NAME = "net.daum.android.map"
