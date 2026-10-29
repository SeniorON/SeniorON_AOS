package com.example.senior_on.ui.child.family.viewmodel

import com.example.senior_on.domain.model.server.ServerPhotoTarget
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyPhotoTargetUiStateTest {
    private val loaded = FamilyPhotoTargetUiState(
        targets = listOf(ServerPhotoTarget(10L, 7L, "시니어", "가족")),
        hasLoaded = true,
    )

    @Test
    fun `이전 대상이 남아 있어도 재조회 중에는 공유할 수 없다`() {
        assertFalse(loaded.copy(isLoading = true).canShare(listOf(10L)))
    }

    @Test
    fun `이전 대상이 남아 있어도 조회 실패 시 공유할 수 없다`() {
        assertFalse(loaded.copy(errorMessage = "조회 실패").canShare(listOf(10L)))
    }

    @Test
    fun `조회 완료 전이나 유효하지 않은 선택은 공유할 수 없다`() {
        assertFalse(loaded.copy(hasLoaded = false).canShare(listOf(10L)))
        assertFalse(loaded.canShare(emptyList()))
        assertFalse(loaded.canShare(listOf(10L, 20L)))
    }

    @Test
    fun `재조회 성공 후 현재 목록의 대상에게만 공유할 수 있다`() {
        assertTrue(loaded.canShare(listOf(10L)))
        val refreshed = loaded.copy(targets = listOf(ServerPhotoTarget(20L, 8L, "시니어", "가족")))
        assertFalse(refreshed.canShare(listOf(10L)))
        assertTrue(refreshed.canShare(listOf(20L)))
    }
}
