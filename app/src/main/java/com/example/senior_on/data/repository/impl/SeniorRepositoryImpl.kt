package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.ManagedSeniorResponse
import com.example.senior_on.data.remote.dto.SeniorRelation
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.source.senior.SeniorDataSource
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.SeniorInfo
import com.example.senior_on.domain.model.senior.SeniorRegistration
import com.example.senior_on.domain.model.senior.SeniorRelationUpdate
import com.example.senior_on.domain.repository.senior.SeniorRepository

class SeniorRepositoryImpl(
    private val dataSource: SeniorDataSource
) : SeniorRepository {
    override suspend fun createSenior(
        familyId: Long,
        accessToken: String?,
        registration: SeniorRegistration
    ): SeniorInfo {
        val response = dataSource.createSenior(
            authorization = accessToken?.toBearerToken(),
            request = CreateSeniorRequest(
                familyId = familyId,
                name = registration.name.trim(),
                relation = registration.relation.toDto(),
                customRelation = registration.customRelation
                    .normalizedCustomRelation(registration.relation),
                birth = registration.birth,
                phoneNumber = registration.phoneNumber.trim(),
                address = registration.address.trim(),
                detailAddress = registration.detailAddress.trim(),
                latitude = registration.latitude,
                longitude = registration.longitude
            )
        )

        return SeniorInfo(
            seniorId = response.seniorId,
            name = response.name,
            relation = response.relation.toDomain(),
            customRelation = response.customRelation,
            birth = response.birth,
            phoneNumber = response.phoneNumber,
            address = response.address,
            detailAddress = response.detailAddress
        )
    }

    override suspend fun getManagedSeniors(): List<ManagedSenior> {
        return dataSource.getManagedSeniors()
            .map { response -> response.toDomain() }
            .distinctBy(ManagedSenior::seniorId)
    }

    override suspend fun updateRelation(
        accessToken: String,
        seniorId: Long,
        relationship: CaregiverRelationship
    ): SeniorRelationUpdate {
        val response = dataSource.updateSeniorRelation(
            authorization = accessToken.toBearerToken(),
            seniorId = seniorId,
            request = UpdateSeniorRelationRequest(
                relation = relationship.relation.toDto(),
                customRelation = relationship.customRelation
                    .normalizedCustomRelation(relationship.relation)
            )
        )

        return SeniorRelationUpdate(
            seniorId = response.seniorId,
            relation = response.relation.toDomain(),
            customRelation = response.customRelation
        )
    }

    private fun SeniorRelationType.toDto(): SeniorRelation {
        return SeniorRelation.valueOf(name)
    }

    private fun SeniorRelation.toDomain(): SeniorRelationType {
        return SeniorRelationType.valueOf(name)
    }

    private fun ManagedSeniorResponse.toDomain(): ManagedSenior {
        val relationship = relation?.let { relation ->
            CaregiverRelationship(
                relation = relation.toDomain(),
                customRelation = customRelation
            )
        } ?: CaregiverRelationship(
            relation = SeniorRelationType.OTHER,
            customRelation = customRelation
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: DEFAULT_MANAGED_SENIOR_RELATIONSHIP_LABEL
        )

        return ManagedSenior(
            familyId = familyId,
            seniorId = seniorId,
            parentUserId = parentUserId,
            name = name,
            relationship = relationship
        )
    }

    private fun String?.normalizedCustomRelation(
        relation: SeniorRelationType
    ): String? {
        return takeIf { relation == SeniorRelationType.OTHER }
            ?.trim()
            ?.takeIf(String::isNotEmpty)
    }

    private fun String.toBearerToken(): String {
        return if (startsWith(BEARER_PREFIX, ignoreCase = true)) {
            this
        } else {
            "$BEARER_PREFIX$this"
        }
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        const val DEFAULT_MANAGED_SENIOR_RELATIONSHIP_LABEL = "부모님"
    }
}
