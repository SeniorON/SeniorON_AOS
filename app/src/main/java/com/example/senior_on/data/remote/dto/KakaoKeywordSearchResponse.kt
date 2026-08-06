package com.example.senior_on.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KakaoKeywordSearchResponse(
    val meta: KakaoKeywordSearchMeta,
    val documents: List<KakaoKeywordDocument>
)

data class KakaoKeywordSearchMeta(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("pageable_count")
    val pageableCount: Int,
    @SerializedName("is_end")
    val isEnd: Boolean
)

data class KakaoKeywordDocument(
    @SerializedName("place_name")
    val placeName: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("address_name")
    val addressName: String,
    @SerializedName("road_address_name")
    val roadAddressName: String,
    val x: String,
    val y: String
)
