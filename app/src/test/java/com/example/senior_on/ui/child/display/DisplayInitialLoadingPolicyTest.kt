package com.example.senior_on.ui.child.display

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplayInitialLoadingPolicyTest {
    @Test
    fun `시니어 목록을 조회하는 동안 초기 로딩을 표시한다`() {
        assertTrue(
            shouldShowDisplayInitialLoading(
                uiState = DisplayTabUiState(),
                isSeniorAccountsLoading = true,
                hasSeniorAccounts = false,
            )
        )
    }

    @Test
    fun `시니어 목록 조회 후 첫 계정 선택 전에도 초기 로딩을 유지한다`() {
        assertTrue(
            shouldShowDisplayInitialLoading(
                uiState = DisplayTabUiState(),
                isSeniorAccountsLoading = false,
                hasSeniorAccounts = true,
            )
        )
    }

    @Test
    fun `화면 개요를 불러오는 동안 초기 로딩을 표시한다`() {
        assertTrue(
            shouldShowDisplayInitialLoading(
                uiState = DisplayTabUiState(
                    selectedSeniorId = 1L,
                    isLoading = true,
                ),
                isSeniorAccountsLoading = false,
                hasSeniorAccounts = true,
            )
        )
    }

    @Test
    fun `화면 개요 조회가 끝나면 초기 로딩을 종료한다`() {
        assertFalse(
            shouldShowDisplayInitialLoading(
                uiState = DisplayTabUiState(
                    selectedSeniorId = 1L,
                    hasLoadedOverview = true,
                ),
                isSeniorAccountsLoading = false,
                hasSeniorAccounts = true,
            )
        )
    }

    @Test
    fun `관리 중인 시니어가 실제로 없으면 빈 상태를 표시할 수 있다`() {
        assertFalse(
            shouldShowDisplayInitialLoading(
                uiState = DisplayTabUiState(),
                isSeniorAccountsLoading = false,
                hasSeniorAccounts = false,
            )
        )
    }
}
