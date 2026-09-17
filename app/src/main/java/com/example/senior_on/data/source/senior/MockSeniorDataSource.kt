package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.ManagedSeniorResponse
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse

class MockSeniorDataSource(
    private val seniorId: Long = DEFAULT_SENIOR_ID,
    initialManagedSeniors: List<ManagedSeniorResponse> = emptyList(),
) : SeniorDataSource {
    private val managedSeniors = initialManagedSeniors.toMutableList()

    override suspend fun createSenior(
        authorization: String?,
        request: CreateSeniorRequest
    ): CreateSeniorResponse {
        val response = CreateSeniorResponse(
            seniorId = seniorId,
            name = request.name,
            relation = request.relation,
            customRelation = request.customRelation,
            birth = request.birth,
            phoneNumber = request.phoneNumber,
            address = request.address,
            detailAddress = request.detailAddress
        )
        upsertManagedSenior(
            ManagedSeniorResponse(
                familyId = request.familyId,
                seniorId = seniorId,
                parentUserId = null,
                name = request.name,
                relation = request.relation,
                customRelation = request.customRelation
            )
        )
        return response
    }

    override suspend fun getManagedSeniors(): List<ManagedSeniorResponse> {
        return managedSeniors.toList()
    }

    override suspend fun updateSeniorRelation(
        authorization: String,
        seniorId: Long,
        request: UpdateSeniorRelationRequest
    ): UpdateSeniorRelationResponse {
        val seniorName = managedSeniors
            .firstOrNull { it.seniorId == seniorId }
            ?.name
            .orEmpty()
        val existing = managedSeniors.firstOrNull { it.seniorId == seniorId }
        upsertManagedSenior(
            ManagedSeniorResponse(
                familyId = existing?.familyId ?: DEFAULT_FAMILY_ID,
                seniorId = seniorId,
                parentUserId = existing?.parentUserId,
                name = seniorName,
                relation = request.relation,
                customRelation = request.customRelation
            )
        )
        return UpdateSeniorRelationResponse(
            seniorId = seniorId,
            relation = request.relation,
            customRelation = request.customRelation
        )
    }

    private fun upsertManagedSenior(senior: ManagedSeniorResponse) {
        managedSeniors.removeAll { it.seniorId == senior.seniorId }
        managedSeniors += senior
    }

    private companion object {
        const val DEFAULT_SENIOR_ID = 1L
        const val DEFAULT_FAMILY_ID = 1L
    }
}
