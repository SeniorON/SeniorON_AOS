package com.example.senior_on.ui.display

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.repository.MockDisplayFixtures
import com.example.senior_on.domain.model.SeniorFontSize
import com.example.senior_on.domain.model.SeniorHomeButtonType
import com.example.senior_on.domain.model.SeniorScreenConfiguration
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun DisplayFontEditScreen(
    initialFontSize: SeniorFontSize,
    buttons: List<SeniorHomeButtonType>,
    customButtonLabels: Map<SeniorHomeButtonType, String> = emptyMap(),
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveClick: (SeniorFontSize) -> Unit = {},
) {
    var selectedFontSize by rememberSaveable(initialFontSize) {
        mutableStateOf(initialFontSize)
    }
    val hasChanges = selectedFontSize != initialFontSize
    val previewConfiguration = remember(
        selectedFontSize,
        buttons,
        customButtonLabels,
    ) {
        SeniorScreenConfiguration(
            fontSize = selectedFontSize,
            buttons = buttons,
            customButtonLabels = customButtonLabels,
        )
    }

    BackHandler(onBack = onBackClick)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background1)
            .statusBarsPadding(),
    ) {
        FontEditTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            FontPreviewCard(configuration = previewConfiguration)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "글씨 크기",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "부모님 화면의 글씨 크기를 조절할 수 있어요",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )

            Spacer(modifier = Modifier.height(12.dp))

            FontSizeSelector(
                selectedFontSize = selectedFontSize,
                onSelect = { selectedFontSize = it },
            )

            Spacer(modifier = Modifier.weight(1f))

            FontEditSaveButton(
                enabled = hasChanges,
                onClick = { onSaveClick(selectedFontSize) },
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FontEditTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(SeniorOnColors.White),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(32.dp)
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
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray800,
            )
        }

        Text(
            text = "글씨 편집",
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800,
        )
    }
}

@Composable
private fun FontPreviewCard(
    configuration: SeniorScreenConfiguration,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(328f / 337f)
            .dropShadow(
                shape = RoundedCornerShape(SeniorOnRadius.Large),
                shadow = Shadow(
                    radius = 12.dp,
                    color = SeniorOnColors.Black.copy(alpha = 0.08f),
                    offset = DpOffset(x = 0.dp, y = 2.dp),
                ),
            )
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White),
    ) {
        Text(
            text = "미리보기",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(111.dp))
                .background(SeniorOnColors.Primary100)
                .border(
                    width = 1.dp,
                    color = SeniorOnColors.Primary600,
                    shape = RoundedCornerShape(111.dp),
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Primary600,
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
        ) {
            SeniorPhonePreview(
                configuration = configuration,
            )
        }
    }
}

@Composable
private fun FontSizeSelector(
    selectedFontSize: SeniorFontSize,
    onSelect: (SeniorFontSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.White),
    ) {
        FontSizeOption(
            label = "크게",
            fontSize = SeniorFontSize.Large,
            selected = selectedFontSize == SeniorFontSize.Large,
            onClick = onSelect,
            modifier = Modifier.weight(1f),
        )
        FontSizeOption(
            label = "보통",
            fontSize = SeniorFontSize.Normal,
            selected = selectedFontSize == SeniorFontSize.Normal,
            onClick = onSelect,
            modifier = Modifier.weight(1f),
        )
        FontSizeOption(
            label = "작게",
            fontSize = SeniorFontSize.Small,
            selected = selectedFontSize == SeniorFontSize.Small,
            onClick = onSelect,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FontSizeOption(
    label: String,
    fontSize: SeniorFontSize,
    selected: Boolean,
    onClick: (SeniorFontSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                if (selected) SeniorOnColors.Primary100 else Color.Transparent
            )
            .then(
                if (selected) {
                    Modifier.border(1.dp, SeniorOnColors.Primary400, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(fontSize) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = SeniorOnTextStyles.ButtonSSemiBold,
            color = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray700,
        )
    }
}

@Composable
private fun FontEditSaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(
                if (enabled) {
                    SeniorOnColors.Primary600
                } else {
                    SeniorOnColors.Primary600.copy(alpha = 0.5f)
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "저장하기",
            style = SeniorOnTextStyles.ButtonM,
            color = SeniorOnColors.White,
        )
    }
}

@Preview(
    name = "Font Edit - Disabled",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun DisplayFontEditScreenPreview() {
    SENIOR_ONTheme {
        DisplayFontEditScreen(
            initialFontSize = MockDisplayFixtures.defaultScreenConfiguration.fontSize,
            buttons = MockDisplayFixtures.defaultScreenConfiguration.buttons,
        )
    }
}
