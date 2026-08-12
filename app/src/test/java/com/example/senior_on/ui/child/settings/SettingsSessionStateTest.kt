package com.example.senior_on.ui.child.settings

import org.junit.Assert.assertNotEquals
import org.junit.Test

class SettingsSessionStateTest {
    @Test
    fun `다른 계정은 서로 다른 설정 프로필 ViewModel 키를 사용한다`() {
        assertNotEquals(
            settingsSessionViewModelKey("user-a:1", "profile"),
            settingsSessionViewModelKey("user-b:2", "profile"),
        )
    }

    @Test
    fun `같은 계정으로 다시 로그인해도 새 설정 프로필 ViewModel 키를 사용한다`() {
        assertNotEquals(
            settingsSessionViewModelKey("user-a:1", "profile"),
            settingsSessionViewModelKey("user-a:2", "profile"),
        )
    }

    @Test
    fun `설정 동작과 프로필 ViewModel은 서로 다른 키를 사용한다`() {
        assertNotEquals(
            settingsSessionViewModelKey("user-a:1", "actions"),
            settingsSessionViewModelKey("user-a:1", "profile"),
        )
    }
}
