package com.example.senior_on.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SeniorWithdrawalNoticeResponse(
    val withdrawalNoticeId: Long,
    val familyId: Long,
    val seniorId: Long,
    val seniorName: String,
    val withdrawnAt: String,
)

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

data class ManagedSeniorResponse(
    val familyId: Long,
    val seniorId: Long?,
    val parentUserId: Long?,
    @SerializedName(value = "seniorName", alternate = ["name"])
    val name: String?,
    val relation: SeniorRelation?,
    val customRelation: String?,
    val withdrawn: Boolean = false,
    val withdrawalNoticeId: Long? = null,
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

data class SeniorFamilyResponse(
    val seniorId: Long,
    val address: String?,
    val detailAddress: String?,
    val latitude: Double?,
    val longitude: Double?,
)
