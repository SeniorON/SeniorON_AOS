package com.example.senior_on.ui.child.family

import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.ui.child.family.viewmodel.SeniorConnectionUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeniorConnectionDestinationTest {
    @Test
    fun `연결 목록을 불러오기 전에는 목적지를 결정하지 않는다`() {
        assertNull(
            resolveSeniorConnectionDestination(
                requestedDestination = null,
                uiState = SeniorConnectionUiState(),
                seniorId = SENIOR_ID,
            )
        )
    }

    @Test
    fun `불러온 연결 목록이 있으면 첫 렌더부터 연결 목록을 표시한다`() {
        val destination = resolveSeniorConnectionDestination(
            requestedDestination = null,
            uiState = SeniorConnectionUiState(
                seniorId = SENIOR_ID,
                connectedSeniors = listOf(CONNECTION),
                hasLoaded = true,
            ),
            seniorId = SENIOR_ID,
        )

        assertEquals(SeniorConnectionDestination.Connected, destination)
    }

    @Test
    fun `불러온 연결 목록이 비어 있으면 소개 화면을 표시한다`() {
        val destination = resolveSeniorConnectionDestination(
            requestedDestination = null,
            uiState = SeniorConnectionUiState(
                seniorId = SENIOR_ID,
                hasLoaded = true,
            ),
            seniorId = SENIOR_ID,
        )

        assertEquals(SeniorConnectionDestination.Intro, destination)
    }

    private companion object {
        const val SENIOR_ID = 7L
        val CONNECTION = ServerConnectedSenior(
            photoGroupId = 101L,
            seniorId = 11L,
            name = "테스트 시니어",
            relationshipLabel = "어머니",
            connectedAt = "2026-10-02T18:00:00",
        )
    }
}
