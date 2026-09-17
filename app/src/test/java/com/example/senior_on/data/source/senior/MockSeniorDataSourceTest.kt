package com.example.senior_on.data.source.senior

import com.example.senior_on.data.remote.dto.CreateSeniorRequest
import com.example.senior_on.data.remote.dto.SeniorRelation
import com.example.senior_on.data.remote.dto.UpdateSeniorRelationRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MockSeniorDataSourceTest {
    @Test
    fun `created senior is exposed through managed senior list with its family`() = runBlocking {
        val source = MockSeniorDataSource(seniorId = 7L)

        source.createSenior(
            authorization = null,
            request = CreateSeniorRequest(
                familyId = 31L,
                name = "김순자",
                relation = SeniorRelation.MOTHER,
                customRelation = null,
                birth = "1958-04-12",
                phoneNumber = "01012345678",
                address = "서울시 종로구",
                detailAddress = "101호",
                latitude = 37.5665,
                longitude = 126.9780,
            ),
        )

        val managedSenior = source.getManagedSeniors().single()
        assertEquals(31L, managedSenior.familyId)
        assertEquals(7L, managedSenior.seniorId)
        assertNull(managedSenior.parentUserId)
        assertEquals("김순자", managedSenior.name)
    }

    @Test
    fun `relation update keeps managed senior family identity`() = runBlocking {
        val source = MockSeniorDataSource(seniorId = 7L)
        source.createSenior(
            authorization = null,
            request = CreateSeniorRequest(
                familyId = 31L,
                name = "김순자",
                relation = SeniorRelation.MOTHER,
                customRelation = null,
                birth = "1958-04-12",
                phoneNumber = "01012345678",
                address = "",
                detailAddress = "",
                latitude = null,
                longitude = null,
            ),
        )

        source.updateSeniorRelation(
            authorization = "Bearer token",
            seniorId = 7L,
            request = UpdateSeniorRelationRequest(
                relation = SeniorRelation.OTHER,
                customRelation = "이모",
            ),
        )

        val managedSenior = source.getManagedSeniors().single()
        assertEquals(31L, managedSenior.familyId)
        assertEquals(SeniorRelation.OTHER, managedSenior.relation)
        assertEquals("이모", managedSenior.customRelation)
    }
}
