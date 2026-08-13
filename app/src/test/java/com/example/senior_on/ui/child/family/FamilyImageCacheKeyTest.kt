package com.example.senior_on.ui.child.family

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FamilyImageCacheKeyTest {
    @Test
    fun `서명 파라미터만 갱신된 구성원 사진은 같은 캐시 키를 사용한다`() {
        val firstUrl = "https://cdn.example.com/profile/member-1.jpg?signature=old"
        val refreshedUrl = "https://cdn.example.com/profile/member-1.jpg?signature=new"

        assertEquals(
            familyMemberImageCacheKey("1", firstUrl),
            familyMemberImageCacheKey("1", refreshedUrl),
        )
    }

    @Test
    fun `구성원 사진 경로가 변경되면 새 캐시 키를 사용한다`() {
        assertNotEquals(
            familyMemberImageCacheKey("1", "https://cdn.example.com/profile/old.jpg"),
            familyMemberImageCacheKey("1", "https://cdn.example.com/profile/new.jpg"),
        )
    }
}
