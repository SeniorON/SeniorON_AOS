package com.example.senior_on.data.model

import com.google.gson.annotations.SerializedName

data class KakaoAddressSearchResponse(
    val meta: KakaoAddressSearchMeta,
    val documents: List<KakaoAddressDocument>
)

data class KakaoAddressSearchMeta(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("pageable_count")
    val pageableCount: Int,
    @SerializedName("is_end")
    val isEnd: Boolean
)

data class KakaoAddressDocument(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("address_type")
    val addressType: String,
    val x: String,
    val y: String,
    val address: KakaoJibunAddress?,
    @SerializedName("road_address")
    val roadAddress: KakaoRoadAddress?
)

data class KakaoJibunAddress(
    @SerializedName("address_name")
    val addressName: String
)

data class KakaoRoadAddress(
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("building_name")
    val buildingName: String,
    @SerializedName("zone_no")
    val zoneNumber: String
)
