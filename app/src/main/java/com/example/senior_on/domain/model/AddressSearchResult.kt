package com.example.senior_on.domain.model

data class AddressSearchResult(
    val primaryAddress: String,
    val secondaryAddress: String?,
    val type: AddressSearchResultType,
    val roadAddress: String?,
    val jibunAddress: String?,
    val buildingName: String?,
    val zoneNumber: String?,
    val longitude: Double?,
    val latitude: Double?
) {
    val selectedAddress: String
        get() = roadAddress ?: jibunAddress ?: primaryAddress

    val needsMoreDetail: Boolean
        get() = type == AddressSearchResultType.Region ||
            type == AddressSearchResultType.Road ||
            type == AddressSearchResultType.Unknown
}

enum class AddressSearchResultType {
    Region,
    Road,
    JibunAddress,
    RoadAddress,
    Unknown
}
