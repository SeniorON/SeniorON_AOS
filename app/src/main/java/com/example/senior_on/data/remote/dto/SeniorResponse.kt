package com.example.senior_on.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateSeniorResponse(
    val seniorId: Long,
    val name: String,
    val relation: SeniorRelation,
    val customRelation: String?,
    val birth: String,
    val phoneNumber: String,
    val address: String,
    val detailAddress: String
)

data class UpdateSeniorRelationResponse(
    val seniorId: Long,
    val relation: SeniorRelation,
    val customRelation: String?
)

enum class SeniorRelation {
    @SerializedName("MOTHER")
    MOTHER,

    @SerializedName("FATHER")
    FATHER,

    @SerializedName("GRANDPARENT")
    GRANDPARENT,

    @SerializedName("OTHER")
    OTHER
}
