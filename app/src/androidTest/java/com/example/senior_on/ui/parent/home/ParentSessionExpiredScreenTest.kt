package com.example.senior_on.ui.parent.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentSessionExpiredScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fallbackKeepsLocalActionsAndExitAvailableWithoutChildMessage() {
        var logins = 0
        var exits = 0
        val clicks = mutableListOf<SeniorHomeButtonType>()
        compose.setContent {
            SENIOR_ONTheme {
                ParentSessionExpiredScreen(
                    now = LocalDateTime.of(2027, 5, 20, 10, 30),
                    onLoginClick = { logins++ },
                    onButtonClick = { clicks.add(it) },
                    onExitHomeClick = { exits++ },
                )
            }
        }
        compose.onNodeWithText("5월 20일 (목)").assertExists()
        compose.onNodeWithText("오전 10:30").assertExists()
        compose.onNodeWithText("로그인이 필요해요").assertExists()
        compose.onNodeWithText("자녀에게 문자 보내기").assertDoesNotExist()
        compose.onNodeWithText("로그인하기").performClick()
        compose.onNodeWithText("전화").performScrollTo().performClick()
        compose.onNodeWithText("메시지").performScrollTo().performClick()
        compose.onNodeWithText("홈 화면으로").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, logins)
            assertEquals(1, exits)
            assertEquals(listOf(SeniorHomeButtonType.Call, SeniorHomeButtonType.Message), clicks)
        }
    }
}
