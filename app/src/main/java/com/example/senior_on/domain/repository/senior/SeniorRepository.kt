package com.example.senior_on.domain.repository.senior

import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.senior.SeniorInfo
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.model.senior.SeniorRelationUpdate

interface SeniorRepository {
    suspend fun createSenior(
        accessToken: String,
        registration: SeniorRegistration
    ): SeniorInfo

    suspend fun updateRelation(
        accessToken: String,
        seniorId: Long,
        relationship: CaregiverRelationship
    ): SeniorRelationUpdate
}
