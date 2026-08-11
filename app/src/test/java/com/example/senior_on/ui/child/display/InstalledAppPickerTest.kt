package com.example.senior_on.ui.child.display

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InstalledAppPickerTest {
    @Test
    fun `액티비티 표시명이 유효하면 우선 사용한다`() {
        assertEquals(
            "넷플릭스",
            selectInstalledAppDisplayName(
                "com.netflix.mediaclient",
                "넷플릭스",
                "Netflix",
            ),
        )
    }

    @Test
    fun `액티비티 표시명이 패키지명이면 앱 표시명을 사용한다`() {
        assertEquals(
            "계산기",
            selectInstalledAppDisplayName(
                "com.sec.android.app.popupcalculator",
                "com.sec.android.app.popupcalculator",
                "계산기",
            ),
        )
    }

    @Test
    fun `모든 후보가 패키지명 형태이면 표시명 조회 실패로 처리한다`() {
        assertNull(
            selectInstalledAppDisplayName(
                "com.sec.android.app.popupcalculator",
                "com.sec.android.app.popupcalculator",
                "com.android.calculator2",
            ),
        )
    }
}
