package com.example.senior_on.ui.onboarding

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.senior_on.ui.onboarding.route.OnboardingProcessScope
import org.junit.Rule
import org.junit.Test

class OnboardingProcessScopeTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sameProcessKeepsStepAndDraft() = exercise(newProcess = false)
    @Test fun newProcessRestartsOnlyOnboardingContent() = exercise(newProcess = true)

    private fun exercise(newProcess: Boolean) {
        var processId = "process-a"
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            OnboardingProcessScope(processId) { restarted ->
                var step by rememberSaveable { mutableStateOf("splash") }
                var draft by rememberSaveable { mutableStateOf("") }
                Button(onClick = { step = "verification"; draft = "draft" }) {
                    Text("$step/$draft/$restarted")
                }
            }
        }
        compose.onNodeWithText("splash//false").performClick()
        compose.onNodeWithText("verification/draft/false").assertExists()
        compose.runOnIdle { if (newProcess) processId = "process-b" }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText(if (newProcess) "splash//true" else "verification/draft/false").assertExists()
    }
}
