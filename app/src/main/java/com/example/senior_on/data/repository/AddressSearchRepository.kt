package com.example.senior_on.data.repository

import com.example.senior_on.BuildConfig
import com.example.senior_on.data.remote.KakaoLocalApi
import com.example.senior_on.data.remote.KakaoLocalNetwork
import com.example.senior_on.data.model.KakaoAddressDocument
import com.example.senior_on.data.model.KakaoCoordinateAddressDocument
import com.example.senior_on.domain.model.AddressSearchResult
import com.example.senior_on.domain.model.AddressSearchResultType

class AddressSearchRepository(
    private val api: KakaoLocalApi = KakaoLocalNetwork.api,
    private val restApiKey: String = BuildConfig.KAKAO_REST_API_KEY
) {
    suspend fun searchAddress(query: String): List<AddressSearchResult> {
        return api.searchAddress(
            authorization = authorizationHeader(),
            query = query
        ).documents.map(KakaoAddressDocument::toSearchResult)
    }

    suspend fun getAddressFromCoordinates(
        latitude: Double,
        longitude: Double
    ): AddressSearchResult? {
        return api.getAddressFromCoordinates(
            authorization = authorizationHeader(),
            longitude = longitude,
            latitude = latitude
        ).documents.firstOrNull()?.toSearchResult(
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun authorizationHeader(): String {
        if (restApiKey.isBlank()) {
            throw MissingKakaoRestApiKeyException()
        }
        return "KakaoAK $restApiKey"
    }
}

class MissingKakaoRestApiKeyException : IllegalStateException()

private fun KakaoAddressDocument.toSearchResult(): AddressSearchResult {
    val jibunAddressName = address?.addressName?.takeIf(String::isNotBlank)
    val roadAddressName = roadAddress?.addressName?.takeIf(String::isNotBlank)
    val buildingName = roadAddress?.buildingName?.takeIf(String::isNotBlank)
    val primaryAddress = jibunAddressName ?: roadAddressName ?: addressName
    val roadAddressWithBuilding = listOfNotNull(roadAddressName, buildingName)
        .distinct()
        .joinToString(" ")
        .takeIf(String::isNotBlank)

    val secondaryAddress = when {
        roadAddressWithBuilding != null && roadAddressName != primaryAddress ->
            "[도로명] $roadAddressWithBuilding"

        jibunAddressName != null && jibunAddressName != primaryAddress ->
            "[지번] $jibunAddressName"

        else -> null
    }

    return AddressSearchResult(
        primaryAddress = primaryAddress,
        secondaryAddress = secondaryAddress,
        type = addressType.toSearchResultType(),
        roadAddress = roadAddressName,
        jibunAddress = jibunAddressName,
        buildingName = buildingName,
        zoneNumber = roadAddress?.zoneNumber?.takeIf(String::isNotBlank),
        longitude = x.toDoubleOrNull(),
        latitude = y.toDoubleOrNull()
    )
}

private fun String.toSearchResultType(): AddressSearchResultType = when (this) {
    "REGION" -> AddressSearchResultType.Region
    "ROAD" -> AddressSearchResultType.Road
    "REGION_ADDR" -> AddressSearchResultType.JibunAddress
    "ROAD_ADDR" -> AddressSearchResultType.RoadAddress
    else -> AddressSearchResultType.Unknown
}

private fun KakaoCoordinateAddressDocument.toSearchResult(
    latitude: Double,
    longitude: Double
): AddressSearchResult {
    val jibunAddressName = address?.addressName?.takeIf(String::isNotBlank)
    val roadAddressName = roadAddress?.addressName?.takeIf(String::isNotBlank)
    val buildingName = roadAddress?.buildingName?.takeIf(String::isNotBlank)
    val primaryAddress = jibunAddressName ?: roadAddressName.orEmpty()
    val roadAddressWithBuilding = listOfNotNull(roadAddressName, buildingName)
        .distinct()
        .joinToString(" ")
        .takeIf(String::isNotBlank)

    return AddressSearchResult(
        primaryAddress = primaryAddress,
        secondaryAddress = roadAddressWithBuilding?.let { "[도로명] $it" },
        type = if (roadAddressName != null) {
            AddressSearchResultType.RoadAddress
        } else {
            AddressSearchResultType.JibunAddress
        },
        roadAddress = roadAddressName,
        jibunAddress = jibunAddressName,
        buildingName = buildingName,
        zoneNumber = roadAddress?.zoneNumber?.takeIf(String::isNotBlank),
        longitude = longitude,
        latitude = latitude
    )
}
