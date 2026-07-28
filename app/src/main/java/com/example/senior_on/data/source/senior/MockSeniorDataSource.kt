package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse

class MockSeniorDataSource(
    private val seniorId: Long = DEFAULT_SENIOR_ID
) : SeniorDataSource {
    override suspend fun createSenior(
        authorization: String,
        request: CreateSeniorRequest
    ): CreateSeniorResponse {
        return CreateSeniorResponse(
            seniorId = seniorId,
            name = request.name,
            relation = request.relation,
            customRelation = request.customRelation,
            birth = request.birth,
            phoneNumber = request.phoneNumber,
            address = request.address,
            detailAddress = request.detailAddress
        )
    }

    override suspend fun updateSeniorRelation(
        authorization: String,
        seniorId: Long,
        request: UpdateSeniorRelationRequest
    ): UpdateSeniorRelationResponse {
        return UpdateSeniorRelationResponse(
            seniorId = seniorId,
            relation = request.relation,
            customRelation = request.customRelation
        )
    }

    private companion object {
        const val DEFAULT_SENIOR_ID = 1L
    }
}
