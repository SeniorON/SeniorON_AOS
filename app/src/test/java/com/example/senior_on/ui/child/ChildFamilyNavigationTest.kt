package com.example.senior_on.ui.child

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `가족 메인에서 사진을 올리면 성공 확인 후 가족 메인으로 돌아간다`() {
        assertEquals(
            ChildFamilyDestination.Overview,
            resolveChildFamilyPhotoUploadSuccessDestination(
                photoShareReturnDestination = ChildFamilyDestination.Overview,
            ),
        )
    }

    @Test
    fun `사진 더보기에서 사진을 올리면 성공 확인 후 사진 목록으로 돌아간다`() {
        assertEquals(
            ChildFamilyDestination.PhotoGallery,
            resolveChildFamilyPhotoUploadSuccessDestination(
                photoShareReturnDestination = ChildFamilyDestination.PhotoGallery,
            ),
        )
    }

    @Test
    fun `사진 업로드 중에는 시스템 상단 하단 내비게이션을 비활성화한다`() {
        assertFalse(isChildMainNavigationEnabled(isFamilyPhotoUploading = true))
    }

    @Test
    fun `사진 업로드 중이 아니면 내비게이션을 활성화한다`() {
        assertTrue(isChildMainNavigationEnabled(isFamilyPhotoUploading = false))
    }
}
