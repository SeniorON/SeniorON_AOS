package com.example.senior_on.data.source.parent

import com.example.senior_on.domain.model.parent.ParentLinkSafetyVerdict
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskLinkVerdictMapperTest {
    @Test
    fun `낮음 응답은 안전으로 변환한다`() {
        assertEquals(ParentLinkSafetyVerdict.Safe, "낮음".toParentLinkSafetyVerdict())
    }

    @Test
    fun `높음 응답은 위험으로 변환한다`() {
        assertEquals(ParentLinkSafetyVerdict.Dangerous, "높음".toParentLinkSafetyVerdict())
    }

    @Test
    fun `확인 불가 또는 빈 응답은 알 수 없음으로 변환한다`() {
        assertEquals(ParentLinkSafetyVerdict.Unknown, "확인불가".toParentLinkSafetyVerdict())
        assertEquals(ParentLinkSafetyVerdict.Unknown, null.toParentLinkSafetyVerdict())
    }
}
