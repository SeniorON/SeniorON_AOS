package com.example.senior_on.data.remote.dto

data class CreateSeniorRequest(
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
    val relation: SeniorRelation,
    val customRelation: String?
)
