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

    @Test
    fun `표시명만 누락되면 직접 이름 입력이 필요하다`() {
        val pickedApp = PickedInstalledApp(
            packageName = "com.example.app",
            label = null,
        )

        assertEquals(true, pickedApp.requiresManualNameInput)
    }

    @Test
    fun `표시명이 있으면 직접 이름 입력이 필요하지 않다`() {
        val pickedApp = PickedInstalledApp(
            packageName = "com.example.app",
            label = "예제 앱",
        )

        assertEquals(false, pickedApp.requiresManualNameInput)
    }

    @Test
    fun `지니뮤직 패키지는 지원 음악 앱으로 판별한다`() {
        val pickedApp = PickedInstalledApp(
            packageName = "com.ktmusic.geniemusic",
            label = "지니뮤직",
        )

        assertEquals(
            com.example.senior_on.domain.model.display.SeniorHomeButtonType.Genie,
            pickedApp.supportedMusicButtonTypeOrNull(),
        )
    }

    @Test
    fun `알려진 패키지가 아니어도 지니뮤직 표시명은 지원 음악 앱으로 판별한다`() {
        val pickedApp = PickedInstalledApp(
            packageName = "com.example.genie.variant",
            label = "지니뮤직",
        )

        assertEquals(
            com.example.senior_on.domain.model.display.SeniorHomeButtonType.Genie,
            pickedApp.supportedMusicButtonTypeOrNull(),
        )
    }

    @Test
    fun `일반 앱은 지원 음악 앱으로 오인하지 않는다`() {
        val pickedApp = PickedInstalledApp(
            packageName = "com.netflix.mediaclient",
            label = "넷플릭스",
        )

        assertNull(pickedApp.supportedMusicButtonTypeOrNull())
    }
}
