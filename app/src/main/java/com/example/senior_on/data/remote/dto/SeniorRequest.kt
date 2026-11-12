package com.example.senior_on.data.remote.dto

data class CreateSeniorRequest(
    val familyId: Long,
    val name: String,
    val relation: SeniorRelation,
    val customRelation: String?,
    val birth: String,
    val phoneNumber: String,
    val address: String,
    val detailAddress: String,
    val latitude: Double?,
    val longitude: Double?
)

data class UpdateSeniorRelationRequest(
    @com.google.gson.annotations.SerializedName("senior_id") val seniorId: Long,
    val relation: SeniorRelation,
    val customRelation: String?
)
