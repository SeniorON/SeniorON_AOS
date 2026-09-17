package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
import com.example.senior_on.data.remote.dto.ManagedSeniorResponse
import com.example.senior_on.data.remote.dto.SeniorRelation
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationResponse
import com.example.senior_on.data.source.senior.SeniorDataSource
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.SeniorRegistration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeniorRepositoryImplTest {
    @Test
    fun `create senior sends family identity and normalized request`() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        repository.createSenior(
            familyId = 31L,
            accessToken = "token",
            registration = seniorRegistration(),
        )

        assertEquals("Bearer token", dataSource.createAuthorization)
        assertEquals(
            CreateSeniorRequest(
                familyId = 31L,
                name = "홍길동",
                relation = SeniorRelation.FATHER,
                customRelation = null,
                birth = "1950-01-02",
                phoneNumber = "010-1234-5678",
                address = "서울시",
                detailAddress = "101호",
                latitude = null,
                longitude = null,
            ),
            dataSource.createRequest,
        )
    }

    @Test
    fun `create senior without explicit token defers auth to interceptor`() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        repository.createSenior(
            familyId = 31L,
            registration = seniorRegistration(),
        )

        assertNull(dataSource.createAuthorization)
    }

    @Test
    fun `managed seniors preserve family and parent account identity`() = runBlocking {
        val dataSource = RecordingSeniorDataSource().apply {
            managedSeniorResponses = listOf(
                ManagedSeniorResponse(
                    familyId = 31L,
                    seniorId = 10L,
                    parentUserId = 99L,
                    name = "김순자",
                    relation = SeniorRelation.MOTHER,
                    customRelation = null,
                ),
            )
        }
        val repository = SeniorRepositoryImpl(dataSource)

        val result = repository.getManagedSeniors().single()

        assertEquals(31L, result.familyId)
        assertEquals(10L, result.seniorId)
        assertEquals(99L, result.parentUserId)
        assertEquals("어머니", result.relationship.displayLabel)
    }

    @Test
    fun `missing relationship uses stable parent fallback`() = runBlocking {
        val dataSource = RecordingSeniorDataSource().apply {
            managedSeniorResponses = listOf(
                ManagedSeniorResponse(
                    familyId = 31L,
                    seniorId = 10L,
                    parentUserId = null,
                    name = "김순자",
                    relation = null,
                    customRelation = null,
                ),
            )
        }
        val repository = SeniorRepositoryImpl(dataSource)

        val result = repository.getManagedSeniors().single()

        assertEquals(SeniorRelationType.OTHER, result.relationship.relation)
        assertEquals("부모님", result.relationship.displayLabel)
    }

    @Test
    fun `update relation preserves token and normalizes custom relationship`() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        repository.updateRelation(
            accessToken = "Bearer token",
            seniorId = 42L,
            relationship = CaregiverRelationship(
                relation = SeniorRelationType.OTHER,
                customRelation = " 이모 ",
            ),
        )

        assertEquals("Bearer token", dataSource.updateAuthorization)
        assertEquals(42L, dataSource.updateSeniorId)
        assertEquals(
            UpdateSeniorRelationRequest(
                relation = SeniorRelation.OTHER,
                customRelation = "이모",
            ),
            dataSource.updateRequest,
        )
    }

    private class RecordingSeniorDataSource : SeniorDataSource {
        var createAuthorization: String? = null
        var createRequest: CreateSeniorRequest? = null
        var managedSeniorResponses: List<ManagedSeniorResponse> = emptyList()
        var updateAuthorization: String? = null
        var updateSeniorId: Long? = null
        var updateRequest: UpdateSeniorRelationRequest? = null

        override suspend fun createSenior(
            authorization: String?,
            request: CreateSeniorRequest,
        ): CreateSeniorResponse {
            createAuthorization = authorization
            createRequest = request
            return CreateSeniorResponse(
                seniorId = 7L,
                name = request.name,
                relation = request.relation,
                customRelation = request.customRelation,
                birth = request.birth,
                phoneNumber = request.phoneNumber,
                address = request.address,
                detailAddress = request.detailAddress,
            )
        }

        override suspend fun getManagedSeniors(): List<ManagedSeniorResponse> =
            managedSeniorResponses

        override suspend fun updateSeniorRelation(
            authorization: String,
            seniorId: Long,
            request: UpdateSeniorRelationRequest,
        ): UpdateSeniorRelationResponse {
            updateAuthorization = authorization
            updateSeniorId = seniorId
            updateRequest = request
            return UpdateSeniorRelationResponse(
                seniorId = seniorId,
                relation = request.relation,
                customRelation = request.customRelation,
            )
        }
    }

    private fun seniorRegistration() = SeniorRegistration(
        name = " 홍길동 ",
        relation = SeniorRelationType.FATHER,
        birth = "1950-01-02",
        phoneNumber = "010-1234-5678",
        address = " 서울시 ",
        detailAddress = " 101호 ",
    )
}
