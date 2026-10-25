package com.example.senior_on.ui.child.display

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayDeviceBannerLabelTest {
    @Test
    fun `로그인 만료 배너는 기기명과 관계를 디자인 형식으로 표시한다`() {
        assertEquals(
            "Galaxy S24-어머니 폰",
            loginExpiredDeviceLabel(
                deviceName = "Galaxy S24",
                relationshipLabel = "어머니",
            ),
        )
    }

    @Test
    fun `관계가 없으면 로그인 만료 배너에 기기명만 표시한다`() {
        assertEquals(
            "Galaxy S24",
            loginExpiredDeviceLabel(
                deviceName = " Galaxy S24 ",
                relationshipLabel = null,
            ),
        )
    }
}
