package com.example.senior_on.ui.common.account

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindAccountComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun passwordFieldKeepsBoundsAcrossFocusAndErrors() {
        val error = mutableStateOf(false)
        compose.setContent {
            SENIOR_ONTheme {
                Column {
                    FindAccountPasswordTextField(
                        label = "새 비밀번호", value = "password123", onValueChange = {},
                        placeholder = "비밀번호 입력", isVisible = false, onVisibilityToggle = {},
                        modifier = Modifier.testTag("password"),
                        isError = error.value,
                        errorMessage = if (error.value) "비밀번호가 일치하지 않아요." else null,
                        reservedSupportingText = "비밀번호가 일치하지 않아요.",
                        showErrorIcon = true,
                    )
                    FindAccountTextField(value = "", onValueChange = {}, placeholder = "다음 입력",
                        modifier = Modifier.testTag("next"))
                }
            }
        }
        val before = compose.onNodeWithTag("next").fetchSemanticsNode().boundsInRoot
        val field = compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("password")))
        field.performClick().assertIsFocused()
        compose.runOnIdle { error.value = true }
        field.assertIsFocused()
        assertEquals(before, compose.onNodeWithTag("next").fetchSemanticsNode().boundsInRoot)
        compose.runOnIdle { error.value = false }
        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("next"))).performClick()
        field.assertIsNotFocused()
        assertEquals(before, compose.onNodeWithTag("next").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun primaryButtonKeepsHeightDuringLoading() {
        val loading = mutableStateOf(false)
        compose.setContent {
            SENIOR_ONTheme {
                FindAccountPrimaryButton("다음", !loading.value, {},
                    modifier = Modifier.testTag("submit"), isLoading = loading.value)
            }
        }
        compose.onNodeWithTag("submit").assertHeightIsEqualTo(50.dp)
        compose.runOnIdle { loading.value = true }
        compose.onNodeWithTag("submit").assertHeightIsEqualTo(50.dp)
        compose.runOnIdle { loading.value = false }
        compose.onNodeWithTag("submit").assertHeightIsEqualTo(50.dp)
    }

    @Test fun fieldKeepsBoundsWhenClearButtonHidesOnFocus() {
        val value = mutableStateOf("테스트 이름")
        compose.setContent {
            SENIOR_ONTheme {
                Column {
                    FindAccountTextField(value = value.value, onValueChange = { value.value = it },
                        placeholder = "이름 입력", modifier = Modifier.testTag("name"))
                    FindAccountTextField(value = "", onValueChange = {}, placeholder = "이메일 입력",
                        modifier = Modifier.testTag("email"))
                }
            }
        }
        val field = compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("name")))
        val before = field.fetchSemanticsNode().boundsInRoot
        compose.onNodeWithContentDescription("입력값 지우기").assertExists()
        field.performClick()
        compose.onNodeWithContentDescription("입력값 지우기").assertDoesNotExist()
        assertEquals(before, field.fetchSemanticsNode().boundsInRoot)
        compose.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag("email"))).performClick()
        compose.onNodeWithContentDescription("입력값 지우기").assertExists()
        assertEquals(before, field.fetchSemanticsNode().boundsInRoot)
    }
}
