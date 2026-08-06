package com.example.senior_on.data.repository.impl

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.CreateSeniorResponse
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
    fun createSenior_mapsDomainToDtoAndResponseBackToDomain() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        val result = repository.createSenior(
            accessToken = "access-token",
            registration = SeniorRegistration(
                name = " 홍길동 ",
                relation = SeniorRelationType.MOTHER,
                customRelation = "사용하지 않음",
                birth = "1950-01-02",
                phoneNumber = " 010-1234-5678 ",
                address = " 서울시 ",
                detailAddress = " 101호 ",
                latitude = 37.5665,
                longitude = 126.9780
            )
        )

        assertEquals("Bearer access-token", dataSource.createAuthorization)
        assertEquals(
            CreateSeniorRequest(
                name = "홍길동",
                relation = SeniorRelation.MOTHER,
                customRelation = null,
                birth = "1950-01-02",
                phoneNumber = "010-1234-5678",
                address = "서울시",
                detailAddress = "101호",
                latitude = 37.5665,
                longitude = 126.9780
            ),
            dataSource.createRequest
        )
        assertEquals(10L, result.seniorId)
        assertEquals(SeniorRelationType.MOTHER, result.relation)
    }

    @Test
    fun updateRelation_preservesBearerTokenAndNormalizesCustomRelation() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        val result = repository.updateRelation(
            accessToken = "Bearer existing-token",
            seniorId = 42L,
            relationship = CaregiverRelationship(
                relation = SeniorRelationType.OTHER,
                customRelation = " 이모 "
            )
        )

        assertEquals("Bearer existing-token", dataSource.updateAuthorization)
        assertEquals(42L, dataSource.updateSeniorId)
        assertEquals(
            UpdateSeniorRelationRequest(
                relation = SeniorRelation.OTHER,
                customRelation = "이모"
            ),
            dataSource.updateRequest
        )
        assertEquals(SeniorRelationType.OTHER, result.relation)
        assertEquals("이모", result.customRelation)
    }

    @Test
    fun updateRelation_dropsCustomRelationForPresetRelation() = runBlocking {
        val dataSource = RecordingSeniorDataSource()
        val repository = SeniorRepositoryImpl(dataSource)

        repository.updateRelation(
            accessToken = "token",
            seniorId = 42L,
            relationship = CaregiverRelationship(
                relation = SeniorRelationType.FATHER,
                customRelation = "잘못된 값"
            )
        )

        assertNull(dataSource.updateRequest?.customRelation)
    }

    private class RecordingSeniorDataSource : SeniorDataSource {
        var createAuthorization: String? = null
        var createRequest: CreateSeniorRequest? = null
        var updateAuthorization: String? = null
        var updateSeniorId: Long? = null
        var updateRequest: UpdateSeniorRelationRequest? = null

        override suspend fun createSenior(
            authorization: String,
            request: CreateSeniorRequest
        ): CreateSeniorResponse {
            createAuthorization = authorization
            createRequest = request
            return CreateSeniorResponse(
                seniorId = 10L,
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
            updateAuthorization = authorization
            updateSeniorId = seniorId
            updateRequest = request
            return UpdateSeniorRelationResponse(
                seniorId = seniorId,
                relation = request.relation,
                customRelation = request.customRelation
            )
        }
    }
}
