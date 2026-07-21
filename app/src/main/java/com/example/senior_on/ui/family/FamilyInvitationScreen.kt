package com.example.senior_on.ui.family

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private const val InvitationCardAspectRatio = 328f / 297f

@Composable
fun FamilyInvitationScreen(
    uiState: FamilyTabUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onKakaoShareClick: () -> Unit = {},
    onMessageShareClick: () -> Unit = {}
) {
    var isCodeCopied by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        FamilyBackTopAppBar(
            title = "가족 추가",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SeniorOnColors.Background1)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            FamilyInvitationHero(memberCount = uiState.members.size)

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_family_invitation_envelope),
                contentDescription = null,
                modifier = Modifier.size(width = 35.dp, height = 28.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            FamilyInvitationCodeTitle()

            Spacer(modifier = Modifier.height(12.dp))

            FamilyInvitationCodeBox(
                code = uiState.invitationCode,
                onCopyClick = {
                    copyFamilyInvitationCode(
                        context = context,
                        invitationCode = uiState.invitationCode
                    )
                    isCodeCopied = true
                }
            )

            if (isCodeCopied) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "코드가 복사되었습니다.",
                    style = SeniorOnTextStyles.CaptionRegular,
                    color = SeniorOnColors.Primary600
                )

                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(28.dp))
            }

            FamilyInvitationShareButtons(
                onKakaoShareClick = onKakaoShareClick,
                onMessageShareClick = onMessageShareClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FamilyInvitationHero(memberCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(InvitationCardAspectRatio)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_family_invitation),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "가족 구성원 추가",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray600
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "가족에게 코드를 공유해주세요",
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 19.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.Medium))
                .background(SeniorOnColors.SupportWhite80)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_big_family),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = SeniorOnColors.Primary600
            )

            Text(
                text = buildAnnotatedString {
                    append("현재 가족 구성원은 ")
                    withStyle(SeniorOnTextStyles.BodyMSemiBold.toSpanStyle()) {
                        append("${memberCount}명")
                    }
                    append("이에요.")
                },
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun FamilyInvitationCodeTitle() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Gray800)) {
                append("나의 ")
            }
            withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                append("가족 공유 코드")
            }
        },
        style = SeniorOnTextStyles.BodyMSemiBold
    )
}

@Composable
private fun FamilyInvitationCodeBox(
    code: String,
    onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(69.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = RoundedCornerShape(SeniorOnRadius.Medium)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = code,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.HeadingXS,
            color = SeniorOnColors.Gray800
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_family_code_copy),
            contentDescription = "가족 공유 코드 복사",
            modifier = Modifier
                .size(34.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCopyClick
                ),
            tint = SeniorOnColors.Gray400
        )
    }
}

@Composable
private fun FamilyInvitationShareButtons(
    onKakaoShareClick: () -> Unit,
    onMessageShareClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_sociallogin_kakao),
            contentDescription = "카카오톡으로 공유",
            modifier = Modifier
                .size(width = 55.dp, height = 54.dp)
                .clickable(onClick = onKakaoShareClick)
        )

        Image(
            painter = painterResource(
                id = R.drawable.ic_family_invitation_message_share
            ),
            contentDescription = "메시지로 공유",
            modifier = Modifier
                .size(54.dp)
                .clickable(onClick = onMessageShareClick)
        )
    }
}

private fun copyFamilyInvitationCode(
    context: Context,
    invitationCode: String
) {
    val clipboardManager = context.getSystemService(
        Context.CLIPBOARD_SERVICE
    ) as ClipboardManager
    val clipData = ClipData.newPlainText("가족 공유 코드", invitationCode)

    clipboardManager.setPrimaryClip(clipData)
}

@Preview(
    name = "Family Invitation",
    showBackground = true,
    widthDp = 360,
    heightDp = 888
)
@Composable
private fun FamilyInvitationScreenPreview() {
    val uiState = MockFamilyFixtures.primaryCaregiverOverview.toFamilyTabUiState()

    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background2)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FamilyInvitationScreen(
                    uiState = uiState,
                    onBackClick = {}
                )
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Family,
                onTabClick = {}
            )
        }
    }
}
