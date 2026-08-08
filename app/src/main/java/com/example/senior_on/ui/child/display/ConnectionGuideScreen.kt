package com.example.senior_on.ui.child.display

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnDimensions
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private const val GuideImageAspectRatio = 984f / 432f

@Composable
fun ConnectionGuideScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    var isSeniorGuideSelected by rememberSaveable { mutableStateOf(false) }

    ConnectionGuideScreenContent(
        isSeniorGuideSelected = isSeniorGuideSelected,
        onBackClick = onBackClick,
        onGuardianGuideClick = { isSeniorGuideSelected = false },
        onSeniorGuideClick = { isSeniorGuideSelected = true },
        modifier = modifier,
    )
}

@Composable
private fun ConnectionGuideScreenContent(
    isSeniorGuideSelected: Boolean,
    onBackClick: () -> Unit,
    onGuardianGuideClick: () -> Unit,
    onSeniorGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding(),
    ) {
        ConnectionGuideTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))

        ConnectionGuideTabs(
            isSeniorGuideSelected = isSeniorGuideSelected,
            onGuardianGuideClick = onGuardianGuideClick,
            onSeniorGuideClick = onSeniorGuideClick,
        )

        if (isSeniorGuideSelected) {
            SeniorConnectionGuideContent(modifier = Modifier.weight(1f))
        } else {
            GuardianConnectionGuideContent(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun GuardianConnectionGuideContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            GuideSectionLabel(text = "주 담당자 등록")

            Spacer(modifier = Modifier.height(24.dp))

            ConnectionGuideStep(
                step = 1,
                title = "시니어ON(보호자) 회원가입",
                imageResId = R.drawable.img_guardian_step01,
                imageDescription = "시니어ON 회원가입 모드 선택 화면",
                description = "앱 실행 후 자녀모드를 선택합니다.\n" +
                    "이후 이름, 이메일, 비밀번호 등을 입력하여 회원가입을 진행합니다.",
            )

            Spacer(modifier = Modifier.height(28.dp))

            ConnectionGuideStep(
                step = 2,
                title = "주 담당자 자동 등록",
                imageResId = R.drawable.img_guardian_step02,
                imageDescription = "주 담당자와 보조 담당자 권한 안내 화면",
                description = "회원가입을 완료하면 자동으로 주 담당자로 등록됩니다. " +
                    "주 담당자는 화면 편집, 구성원·권한 관리 등 모든 기능을 이용할 수 있습니다.",
            )

            Spacer(modifier = Modifier.height(28.dp))

            ConnectionGuideStep(
                step = 3,
                title = "가족코드 확인",
                imageResId = R.drawable.img_guardian_step03,
                imageDescription = "가족 공유 코드 확인 화면",
                description = "[가족탭] > [가족추가] 버튼을 눌러 가족코드를 확인합니다.",
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(SeniorOnColors.Background3),
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            GuideSectionLabel(text = "보조 담당자 추가")

            Spacer(modifier = Modifier.height(24.dp))

            ConnectionGuideStep(
                step = 4,
                title = "다른 보호자(배우자, 형제자매 등) 초대",
                imageResId = R.drawable.img_guardian_step04,
                imageDescription = "가족 공유 코드 입력 화면",
                description = "발급된 가족코드를 다른 가족 구성원(배우자, 형제자매 등)에게 " +
                    "전달합니다. 동일한 가족코드로 연결하면 같은 가족의 보조 담당자로 등록됩니다.",
            )

            Spacer(modifier = Modifier.height(28.dp))

            ConnectionGuideStep(
                step = 5,
                title = "시니어와의 관계 선택",
                imageResId = R.drawable.img_guardian_step05,
                imageDescription = "시니어와의 관계 선택 화면",
                description = "가족코드 입력 후, 등록된 시니어와 어떤 관계인지 선택합니다. " +
                    "어머니·아버지·조부모 중 선택하거나, 해당하지 않는 경우 ‘직접 작성’으로 " +
                    "입력할 수 있습니다. 이 정보는 나중에 설정에서 변경할 수 있습니다.",
            )

            Spacer(modifier = Modifier.height(28.dp))

            ConnectionGuideStepHeader(
                step = 6,
                title = "보조 담당자 권한 안내",
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "회원가입 완료 후 보조 담당자로 연결되면 알림 받기, 기록 보기, " +
                    "사진 공유, 화면 등록 등의 기능을 이용할 수 있습니다.",
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray800,
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = SeniorOnColors.Gray100,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_information),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SeniorOnColors.Gray800,
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "주 담당자만 이용할 수 있는 기능이에요.",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Gray700,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GuidePermissionChip(text = "부모님 홈 화면 버튼 편집")

            Spacer(modifier = Modifier.height(8.dp))

            GuidePermissionChip(text = "구성원 관리 및 권한 변경·삭제")

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SeniorConnectionGuideContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        GuideSectionLabel(text = "주 담당자 등록")

        Spacer(modifier = Modifier.height(24.dp))

        ConnectionGuideStep(
            step = 1,
            title = "시니어ON 실행 후 ‘시니어’ 모드 선택",
            imageResId = R.drawable.img_senior_step01,
            imageDescription = "시니어ON 시니어 모드 선택 화면",
            description = "앱을 처음 실행하면 역할 선택 화면이 나타납니다.\n" +
                "반드시 ‘시니어’ 모드를 선택해야 합니다.",
            reference = "‘보호자’ 모드를 선택하지 않도록 주의해 주세요. " +
                "보호자와 시니어는 서로 다른 화면으로 연결됩니다.",
        )

        Spacer(modifier = Modifier.height(28.dp))

        ConnectionGuideStep(
            step = 2,
            title = "시니어ON (시니어용) 회원가입",
            imageResId = R.drawable.img_senior_step02,
            imageDescription = "시니어용 회원가입 정보 입력 화면",
            description = "시니어 모드로 진입 후, 이름, 전화번호(또는 이메일) 등 기본 정보를 " +
                "입력하여 회원가입을 진행합니다.",
        )

        Spacer(modifier = Modifier.height(28.dp))

        ConnectionGuideStep(
            step = 3,
            title = "가족코드 입력",
            imageResId = R.drawable.img_senior_step03,
            imageDescription = "가족 공유 코드 입력 화면",
            description = "보호자(자녀)에게 전달받은 가족코드를 입력합니다.",
            reference = "가족코드는 보호자의 [가족탭] > [가족추가]에서 확인할 수 있습니다.\n" +
                "코드를 모르는 경우 보호자에게 다시 문의해 주세요.",
        )

        Spacer(modifier = Modifier.height(28.dp))

        ConnectionGuideStep(
            step = 4,
            title = "이용약관 동의",
            imageResId = R.drawable.img_senior_step04,
            imageDescription = "회원가입 이용약관 동의 화면",
            description = "서비스 이용을 위한 약관 내용을 확인하고 동의합니다.",
        )

        Spacer(modifier = Modifier.height(28.dp))

        ConnectionGuideStep(
            step = 5,
            title = "가족 연결 완료",
            imageResId = R.drawable.img_senior_step05,
            imageDescription = "가족 연결이 완료된 시니어 홈 화면",
            description = "가족코드 연결이 완료되면, 보호자가 미리 구성해 둔 홈 화면이 " +
                "자동으로 표시됩니다.",
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ConnectionGuideTopBar(
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SeniorOnDimensions.TopBarHeight)
            .background(SeniorOnColors.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.size(26.dp),
                    tint = SeniorOnColors.Gray800,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_senior_on_logo),
                contentDescription = "시니어ON",
                modifier = Modifier.size(width = 96.dp, height = 24.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.06f),
                            Color.Black.copy(alpha = 0.025f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ConnectionGuideTabs(
    isSeniorGuideSelected: Boolean,
    onGuardianGuideClick: () -> Unit,
    onSeniorGuideClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(horizontal = 16.dp),
    ) {
        GuideTab(
            text = "보호자 연결",
            selected = !isSeniorGuideSelected,
            onClick = onGuardianGuideClick,
            modifier = Modifier.weight(1f),
        )
        GuideTab(
            text = "시니어 연결",
            selected = isSeniorGuideSelected,
            onClick = onSeniorGuideClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun GuideTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (selected) SeniorOnColors.White else SeniorOnColors.Background1,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray300,
            textAlign = TextAlign.Center,
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(SeniorOnColors.Primary600),
            )
        }
    }
}

@Composable
private fun GuideSectionLabel(
    text: String,
) {
    Box(
        modifier = Modifier
            .size(width = 98.dp, height = 29.dp)
            .background(
                color = SeniorOnColors.Primary600,
                shape = RoundedCornerShape(SeniorOnRadius.Small),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ConnectionGuideStep(
    step: Int,
    title: String,
    @DrawableRes imageResId: Int,
    imageDescription: String,
    description: String,
    reference: String? = null,
) {
    ConnectionGuideStepHeader(
        step = step,
        title = title,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Image(
        painter = painterResource(id = imageResId),
        contentDescription = imageDescription,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(GuideImageAspectRatio),
        contentScale = ContentScale.Fit,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = description,
        style = SeniorOnTextStyles.CaptionRegular,
        color = SeniorOnColors.Gray800,
    )

    if (reference != null) {
        Spacer(modifier = Modifier.height(12.dp))

        GuideReferenceNote(text = reference)
    }
}

@Composable
private fun GuideReferenceNote(
    text: String,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_information),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SeniorOnColors.Red400,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "참고:",
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Red400,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = text,
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Gray600,
        )
    }
}

@Composable
private fun ConnectionGuideStepHeader(
    step: Int,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(18.dp)
                .background(SeniorOnColors.Primary600),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "STEP ${step.toString().padStart(2, '0')}",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Primary700,
        )
    }

    Spacer(modifier = Modifier.height(15.dp))

    Text(
        text = title,
        style = SeniorOnTextStyles.BodySSemiBold,
        color = SeniorOnColors.Gray800,
    )
}

@Composable
private fun GuidePermissionChip(
    text: String,
) {
    Box(
        modifier = Modifier
            .background(
                color = SeniorOnColors.Background2,
                shape = RoundedCornerShape(SeniorOnRadius.Small),
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Primary700,
        )
    }
}

@Preview(
    name = "Guardian connection guide",
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun ConnectionGuideScreenPreview() {
    SENIOR_ONTheme {
        ConnectionGuideScreen()
    }
}

@Preview(
    name = "Senior connection guide",
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SeniorConnectionGuideScreenPreview() {
    SENIOR_ONTheme {
        ConnectionGuideScreenContent(
            isSeniorGuideSelected = true,
            onBackClick = {},
            onGuardianGuideClick = {},
            onSeniorGuideClick = {},
        )
    }
}
