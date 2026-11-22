package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.display.DisplayDevice
import com.example.senior_on.domain.model.display.DisplayDeviceConnectionStatus
import com.example.senior_on.domain.model.parent.ParentInfo
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.common.seniorinfo.*
import com.example.senior_on.ui.onboarding.familycode.*
import java.time.LocalDate

// Use screen composables, never routes: the guide has no repository or real family/session.
private val exampleParent = ParentInfo(0L, "김순자", "어머니", LocalDate.of(1958,4,12), "", "")
private val exampleDevice = DisplayDevice("demo", "시니어 폰", DisplayDeviceConnectionStatus.Online, 76)
private const val exampleCode = "43TS-6GTE"

@Composable
internal fun TutorialSceneContent(scene: TutorialScene, anchor: Modifier) {
    when (scene) {
        TutorialScene.Start, TutorialScene.Connect, TutorialScene.Connected -> Column {
            DisplayTabScreen(
                uiState = DisplayTabUiState(parentInfo = exampleParent, relationshipLabel = "어머니",
                    device = exampleDevice.takeIf { scene == TutorialScene.Connected }),
                modifier = Modifier.weight(1f),
                emptySeniorList = scene == TutorialScene.Start,
                tutorialAnchorModifier = if (scene == TutorialScene.Start) anchor else Modifier,
                deviceAnchor = if (scene != TutorialScene.Start) anchor else Modifier,
            )
            ChildBottomNavigation(ChildMainTab.Screen, {})
        }
        TutorialScene.NoCode, TutorialScene.RequestCode -> FamilyShareCodeScreenContent(
            topBarTitle = "시니어 추가",
            selectedOption = FamilyShareCodeOption.NoCode.takeIf { scene == TutorialScene.RequestCode },
            optionAnchor = if (scene == TutorialScene.NoCode) anchor else Modifier,
            nextAnchor = if (scene == TutorialScene.RequestCode) anchor else Modifier,
            onOptionSelected = {}, onBackClick = {}, onNextClick = {},
        )
        TutorialScene.Issued -> FamilyShareCodeCreatedScreen(
            onBackClick = {}, onNextClick = {}, topBarTitle = "시니어 추가",
            familyShareCode = exampleCode, codeAnchor = anchor,
        )
        TutorialScene.Profile -> ParentInfoInputScreen(
            initialState = ParentInfoInputState("김순자", SeniorRelationship.Mother, "1958.04.12",
                "01000000000", "", "", null, null), saveAnchor = anchor,
        )
        TutorialScene.Share -> SeniorAppInstallGuideScreen(sendAnchor = anchor)
        TutorialScene.EnterCode -> FamilyShareCodeInputScreen(
            onBackClick = {}, onLoginClick = {}, topBarTitle = "시니어 코드 입력", codeAnchor = anchor,
        )
        TutorialScene.Message -> ExampleKakaoChat(anchor)
    }
}

/** Preview of the post-launch invitation; intentionally has no URL or sharing action. */
@Composable
private fun ExampleKakaoChat(messageAnchor: Modifier) {
    Surface(color = Color(0xffb2c7d9), contentColor = Color(0xff25282b)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Text("←  우리 딸", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painterResource(R.drawable.img_default_profile), contentDescription = null, modifier = Modifier.size(38.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("우리 딸", style = MaterialTheme.typography.labelMedium)
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White, contentColor = Color.Black) {
                        Text("엄마, 아래 ‘설치하기’를 눌러 주세요! 시니어 코드는 ${exampleCode}예요 😊",
                            Modifier.padding(10.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Surface(modifier = Modifier.widthIn(max = 360.dp).then(messageAnchor),
                        shape = RoundedCornerShape(12.dp), color = Color.White, contentColor = Color.Black) {
                        Column {
                            Box(Modifier.fillMaxWidth().height(96.dp).background(Color(0xffe8f2d6)),
                                contentAlignment = Alignment.Center) {
                                Text("시니어On", color = Color(0xff58752d), style = MaterialTheme.typography.headlineSmall)
                            }
                            Text("가족이 시니어On에 초대했어요.\n앱을 설치한 뒤 로그인해 주세요.",
                                Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Box(Modifier.fillMaxWidth().heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                                Text("설치하기", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
            Text("메시지 입력", Modifier.fillMaxWidth().background(Color.White).padding(14.dp),
                color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)
        }
    }
}
