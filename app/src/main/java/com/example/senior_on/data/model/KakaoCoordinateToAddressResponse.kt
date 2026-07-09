package com.example.senior_on.data.model

import com.google.gson.annotations.SerializedName

data class KakaoCoordinateToAddressResponse(
    val meta: KakaoCoordinateToAddressMeta,
    val documents: List<KakaoCoordinateAddressDocument>
)

data class KakaoCoordinateToAddressMeta(
    @SerializedName("total_count")
    val totalCount: Int
)

data class KakaoCoordinateAddressDocument(
    val address: KakaoCoordinateJibunAddress?,
    @SerializedName("road_address")
    val roadAddress: KakaoCoordinateRoadAddress?
)

data class KakaoCoordinateJibunAddress(
    @SerializedName("address_name")
    val addressName: String
)

data class KakaoCoordinateRoadAddress(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("building_name")
    val buildingName: String,
    @SerializedName("zone_no")
    val zoneNumber: String
)
