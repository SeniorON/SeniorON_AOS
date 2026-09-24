package com.example.senior_on.ui.parent.settings

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.senior_on.ui.parent.permission.*
import com.example.senior_on.ui.parent.settings.account.ParentChangeNameScreen
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentSettingsScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun parentSettingsNavigateWithoutChangingAccount() {
        compose.setContent { SENIOR_ONTheme { ParentSettingsRoute({}) } }
        compose.onNodeWithText("내 계정").performClick()
        compose.onNodeWithText("이메일").assertExists()
        compose.onNodeWithText("이름 변경").performClick()
        compose.onNodeWithText("저장하기").assertIsNotEnabled()
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithText("이메일").assertExists()
        compose.onNodeWithContentDescription("뒤로가기").performClick()
        compose.onNodeWithText("자녀와 연결 해제").performClick()
        compose.onNodeWithText("자녀와의 연결이 끊어져요.").assertExists()
        compose.onNodeWithText("취소").performClick()
        compose.onNodeWithText("공유코드 확인").performClick()
        compose.onNodeWithContentDescription("공유코드 복사").assertIsNotEnabled()
    }

    @Test fun deniedPermissionStaysOnItsStepAndAllowsLeaving() {
        var exits = 0
        val platform = FakePermissionController()
        compose.setContent { SENIOR_ONTheme { ParentPermissionGuideRoute({ exits++ }, controller = platform) } }
        compose.onNodeWithText("설정하기").performClick()
        compose.onNodeWithText("1", substring = false).assertExists()
        compose.onNodeWithText("아직 설정이 확인되지 않았어요.", substring = true).assertExists()
        compose.onNodeWithText("나중에 설정하기").performClick()
        compose.runOnIdle { assertEquals(1, exits) }
    }

    @Test fun grantedStepsAdvanceButSamsungRequiresManualConfirmation() {
        val platform = FakePermissionController(grantOnRequest = true)
        compose.setContent { SENIOR_ONTheme { ParentPermissionGuideRoute({}, controller = platform) } }
        ParentPermissionStep.entries.forEach { step ->
            compose.onNodeWithText("${step.ordinal + 1}", substring = false).assertExists()
            compose.onNodeWithText(step.guideContent().button).performClick()
        }
        compose.onNodeWithText("절전 앱 제외 확인").assertExists()
        compose.onNodeWithText("아직 안 했어요").performClick()
        compose.onNodeWithText("6", substring = false).assertExists()
        compose.onNodeWithText("설정하기").performClick()
        compose.onNodeWithText("직접 설정했어요").performClick()
        compose.onNodeWithText("설정 완료!").assertExists()
        compose.onNodeWithText("시작하기").assertExists()
    }

    @Test fun nameFieldEnablesOnlyForChangedName() {
        compose.setContent { SENIOR_ONTheme { ParentChangeNameScreen("김순자", {}, {}) } }
        compose.onNodeWithText("저장하기").assertIsNotEnabled()
        compose.onNodeWithText("새 이름 입력").performTextInput("김미애")
        compose.onNodeWithText("저장하기").assertIsEnabled()
    }

    @Test fun logoutUsesSharedConfirmationAndCancelDoesNotConfirm() {
        var confirmations = 0
        var dismissals = 0
        compose.setContent { SENIOR_ONTheme {
            ParentSettingsConfirmDialog(ParentSettingsConfirmation.Logout, { dismissals++ }, { confirmations++ })
        } }
        compose.onNodeWithText("로그아웃 할까요?").assertExists()
        compose.onNodeWithText("로그인 화면으로 이동해요").assertExists()
        compose.onNodeWithText("취소").performClick()
        compose.runOnIdle { assertEquals(0, confirmations); assertEquals(1, dismissals) }
    }

    @Test fun withdrawalUsesSameCopyAsChildConfirmation() {
        compose.setContent { SENIOR_ONTheme {
            ParentSettingsConfirmDialog(ParentSettingsConfirmation.Withdraw, {}, {})
        } }
        compose.onNodeWithText("정말 탈퇴하시겠어요?").assertExists()
        compose.onNodeWithText("탈퇴 후에는 모든 데이터가\n 복구되지 않아요").assertExists()
    }

    @Test fun captureSettingsDesign() {
        compose.setContent { SENIOR_ONTheme { ParentSettingsScreen(ParentSettingsProfile("김순자", "Kim@email.com"), {}, {}, {}, {}) } }
        capture("parent-settings")
    }

    @Test fun capturePermissionDesign() {
        compose.setContent { SENIOR_ONTheme { ParentPermissionGuideScreen(ParentPermissionStep.BatteryOptimization, {}, {}) } }
        capture("parent-permission")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "parent-ui-verification").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private class FakePermissionController(private val grantOnRequest: Boolean = false) : ParentPermissionController {
        private val granted = mutableSetOf<ParentPermissionStep>()
        override fun status(step: ParentPermissionStep) = when {
            step == ParentPermissionStep.SleepingApps -> ParentPermissionStatus.Manual
            step in granted -> ParentPermissionStatus.Granted
            else -> ParentPermissionStatus.Required
        }
        override fun request(step: ParentPermissionStep, permissions: (Array<String>) -> Unit, settings: (android.content.Intent) -> Unit) {
            if (grantOnRequest) granted += step
            // Empty permission contract completes without changing device permissions.
            permissions(emptyArray())
        }
    }
}
