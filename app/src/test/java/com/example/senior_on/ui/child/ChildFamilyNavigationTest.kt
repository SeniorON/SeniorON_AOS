package com.example.senior_on.ui.child

import org.junit.Assert.assertEquals
import org.junit.Test

class ChildFamilyNavigationTest {
    @Test
    fun `메인에서 선택한 사진 공유 화면은 뒤로가기 시 가족 메인으로 돌아간다`() {
        assertEquals(
            ChildFamilyDestination.Overview,
            resolveChildFamilyBackDestination(
                currentDestination = ChildFamilyDestination.PhotoShare,
                photoShareReturnDestination = ChildFamilyDestination.Overview,
            ),
        )
    }

    @Test
    fun `사진 더보기에서 선택한 사진 공유 화면은 뒤로가기 시 사진 목록으로 돌아간다`() {
        assertEquals(
            ChildFamilyDestination.PhotoGallery,
            resolveChildFamilyBackDestination(
                currentDestination = ChildFamilyDestination.PhotoShare,
                photoShareReturnDestination = ChildFamilyDestination.PhotoGallery,
            ),
        )
    }
}
