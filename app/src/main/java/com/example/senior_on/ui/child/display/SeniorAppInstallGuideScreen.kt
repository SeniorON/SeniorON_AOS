package com.example.senior_on.ui.child.display

import com.example.senior_on.ui.theme.SeniorOnDimensions
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.common.seniorinfo.SeniorInfoActionButton
import com.example.senior_on.ui.common.seniorinfo.SeniorInfoButtonStyle
import com.example.senior_on.ui.common.seniorinfo.SeniorInfoPhoneTextField
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SeniorAppInstallGuideScreen(
    phoneNumber: TextFieldValue,
    onPhoneNumberChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSendInstallLinkClick: () -> Unit = {},
    onAppInstallMethodClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SeniorAppInstallGuideTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Image(
                painter = painterResource(
                    id = R.drawable.img_senior_app_install_guide
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(140.dp)
                    .height(136.dp),
            )

            Spacer(modifier = Modifier.height(37.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = SeniorOnColors.Primary700)) {
                        append("시니어 앱")
                    }
                    append(" 설치 및 로그인이 필요해요")
                },
                modifier = Modifier.fillMaxWidth(),
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = "시니어 기기에 설치링크를 메시지로 전송하여,\n" +
                    "시니어ON 설치 후 로그인 해주세요.",
                modifier = Modifier.fillMaxWidth(),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray600,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "시니어의 전화번호(-를 제외하고 입력하세요)",
                modifier = Modifier.fillMaxWidth(),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray600,
            )

            Spacer(modifier = Modifier.height(6.dp))

            SeniorInfoPhoneTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = "01012345678",
                onClearClick = {
                    onPhoneNumberChange(TextFieldValue())
                },
                textStyle = SeniorOnTextStyles.BodyMMedium,
                showFocusedBorder = false,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SeniorInfoActionButton(
                text = "설치링크 전송하기",
                onClick = onSendInstallLinkClick,
                style = SeniorInfoButtonStyle.Filled,
                height = 50.dp,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(48.dp))

            AppInstallMethodChip(
                onClick = onAppInstallMethodClick,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AppInstallMethodChip(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(SeniorOnColors.Gray100)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_information),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SeniorOnColors.Gray500,
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = "앱 설치 방법",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray600,
        )
    }
}

@Composable
private fun SeniorAppInstallGuideTopBar(
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SeniorOnDimensions.TopBarHeight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null,
                    onClick = onBackClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(26.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

@Preview(
    name = "Senior app install guide",
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SeniorAppInstallGuideScreenPreview() {
    var phoneNumber by remember {
        mutableStateOf(TextFieldValue("01012345678"))
    }

    SENIOR_ONTheme {
        SeniorAppInstallGuideScreen(
            phoneNumber = phoneNumber,
            onPhoneNumberChange = { phoneNumber = it },
        )
    }
}
