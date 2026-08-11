package com.example.senior_on.domain.model.senior

import com.example.senior_on.domain.model.parent.SeniorRelationType

data class SeniorRegistration(
    val name: String,
    val relation: SeniorRelationType,
    val customRelation: String? = null,
    val birth: String,
    val phoneNumber: String,
    val address: String,
    val detailAddress: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SeniorInfo(
    val seniorId: Long,
    val name: String,
    val relation: SeniorRelationType,
    val customRelation: String?,
    val birth: String,
    val phoneNumber: String,
    val address: String,
    val detailAddress: String
)

data class SeniorRelationUpdate(
    val seniorId: Long,
    val relation: SeniorRelationType,
    val customRelation: String?
)
