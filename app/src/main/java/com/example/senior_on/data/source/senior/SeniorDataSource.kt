package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse

interface SeniorDataSource {
    suspend fun createSenior(
        authorization: String,
        request: CreateSeniorRequest
    ): CreateSeniorResponse

    suspend fun updateSeniorRelation(
        authorization: String,
        seniorId: Long,
        request: UpdateSeniorRelationRequest
    ): UpdateSeniorRelationResponse
}
