package com.example.senior_on.ui.consent

import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.senior_on.data.consent.GuardianAccess
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test

class GuardianHealthGateTest {
    @get:Rule val compose = createComposeRule()

    @Test fun restoreOnlyAfterFreshApprovalAndForgetOnRevocationOrSwitch() {
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry.createUnsafe(this)
        }
        var senior by mutableLongStateOf(1L)
        var result = CompletableDeferred<GuardianAccess>()
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                GuardianHealthGate("session", senior, senior, Modifier, { result.await() }) {
                    var input by rememberSaveable { mutableStateOf("") }
                    TextField(input, { input = it }, Modifier.testTag("draft"))
                }
            }
        }
        fun resume() = compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        fun pause() = compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.STARTED }
        fun pending() { result = CompletableDeferred() }
        val field = compose.onNodeWithTag("draft")
        resume()
        field.assertDoesNotExist()
        result.complete(GuardianAccess.NotConnected)
        compose.onNodeWithText("현재 연결된 시니어 계정이 없어요.").assertIsDisplayed()
        field.assertDoesNotExist()
        pause()
        pending()
        resume()
        result.complete(GuardianAccess.Allowed)
        field.performTextInput("병원")
        pause()
        pending()
        resume()
        field.assertDoesNotExist()
        result.completeExceptionally(IllegalStateException())
        field.assertDoesNotExist()
        pending()
        compose.onNodeWithText("다시 시도").performClick()
        field.assertDoesNotExist()
        result.complete(GuardianAccess.Allowed)
        field.assertTextEquals("병원")
        pause()
        pending()
        resume()
        result.complete(GuardianAccess.Denied)
        field.assertDoesNotExist()
        compose.onNodeWithText("시니어가 건강정보 이용에 동의하면 확인할 수 있어요").assertExists()
        pause()
        pending()
        resume()
        result.complete(GuardianAccess.Allowed)
        field.assertTextEquals("")
        field.performTextInput("이전 시니어")
        compose.runOnIdle { pending(); senior = 2L }
        field.assertDoesNotExist()
        result.complete(GuardianAccess.Allowed)
        field.assertTextEquals("")
    }
}
