package com.example.senior_on.domain.model.family

data class FamilyJoinResult(
    val familyId: Long,
    val familyCode: String,
    val memberRole: FamilyMemberRole,
)
