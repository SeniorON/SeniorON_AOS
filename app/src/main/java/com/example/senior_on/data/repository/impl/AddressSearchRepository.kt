package com.example.senior_on.data.repository.impl

import com.example.senior_on.BuildConfig
import com.example.senior_on.data.remote.api.KakaoLocalApi
import com.example.senior_on.data.remote.api.KakaoLocalNetwork
import com.example.senior_on.data.remote.dto.KakaoAddressDocument
import com.example.senior_on.data.remote.dto.KakaoCoordinateAddressDocument
import com.example.senior_on.data.remote.dto.KakaoKeywordDocument
import com.example.senior_on.domain.model.address.AddressSearchResult
import com.example.senior_on.domain.model.address.AddressSearchResultType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AddressSearchRepository(
    private val api: KakaoLocalApi = KakaoLocalNetwork.api,
    private val restApiKey: String = BuildConfig.KAKAO_REST_API_KEY
) {
    suspend fun searchAddress(query: String): List<AddressSearchResult> = coroutineScope {
        val authorization = authorizationHeader()
        val addressResults = async {
            api.searchAddress(
                authorization = authorization,
                query = query
            ).documents.map(KakaoAddressDocument::toSearchResult)
        }
        val keywordResults = async {
            api.searchKeyword(
                authorization = authorization,
                query = query
            ).documents.map(KakaoKeywordDocument::toSearchResult)
        }

        (addressResults.await() + keywordResults.await())
            .distinctBy(AddressSearchResult::deduplicationKey)
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

private fun KakaoKeywordDocument.toSearchResult(): AddressSearchResult {
    val roadAddress = roadAddressName.takeIf(String::isNotBlank)
    val jibunAddress = addressName.takeIf(String::isNotBlank)
    val place = placeName.takeIf(String::isNotBlank)
    val address = roadAddress ?: jibunAddress.orEmpty()
    val secondaryAddress = listOfNotNull(
        address.takeIf { it != place },
        categoryName.takeIf(String::isNotBlank)
    ).distinct().joinToString(" · ").takeIf(String::isNotBlank)

    return AddressSearchResult(
        primaryAddress = place ?: address,
        secondaryAddress = secondaryAddress,
        type = when {
            roadAddress != null -> AddressSearchResultType.RoadAddress
            jibunAddress != null -> AddressSearchResultType.JibunAddress
            else -> AddressSearchResultType.Unknown
        },
        roadAddress = roadAddress,
        jibunAddress = jibunAddress,
        buildingName = place,
        zoneNumber = null,
        longitude = x.toDoubleOrNull(),
        latitude = y.toDoubleOrNull()
    )
}

private fun AddressSearchResult.deduplicationKey(): String {
    val normalizedAddress = selectedAddress
        .replace(" ", "")
        .lowercase()
    val normalizedLongitude = longitude?.let { "%.6f".format(java.util.Locale.US, it) }.orEmpty()
    val normalizedLatitude = latitude?.let { "%.6f".format(java.util.Locale.US, it) }.orEmpty()
    return "$normalizedAddress|$normalizedLongitude|$normalizedLatitude"
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
