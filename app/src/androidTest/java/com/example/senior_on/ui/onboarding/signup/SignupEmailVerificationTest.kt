package com.example.senior_on.ui.onboarding.signup

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.senior_on.domain.model.auth.VerificationRequestGate
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class SignupEmailVerificationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun verifiedCodeIsLockedAndChangingEmailInvalidatesVerification() {
        val email = "qa${System.nanoTime()}@example.com"
        compose.setContent {
            SENIOR_ONTheme {
                SignupEmailVerificationScreen(
                    onBackClick = {}, onNextClick = {},
                    onSendVerificationCode = { address, result ->
                        runBlocking {
                            VerificationRequestGate.send(VerificationRequestGate.emailKey(address), { it }) { true }
                        }
                        result(true)
                    },
                    onVerifyCode = { _, _, result -> result(true) },
                )
            }
        }
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(email)
        compose.onNodeWithText("인증 요청").performClick()
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("123456")
        compose.onNodeWithText("인증하기").performClick()
        compose.onAllNodes(hasSetTextAction())[1].assertIsNotEnabled()
        compose.onNodeWithText("인증완료").assertIsNotEnabled()
        compose.onNodeWithText("다음").assertIsEnabled()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("new$email")
        compose.onNodeWithText("다음").assertIsNotEnabled()
        compose.onNodeWithText("인증 요청").assertIsEnabled().performClick()
        compose.onAllNodes(hasSetTextAction())[1].assertIsEnabled()
        compose.onNodeWithText("다음").assertIsNotEnabled()
    }
}
