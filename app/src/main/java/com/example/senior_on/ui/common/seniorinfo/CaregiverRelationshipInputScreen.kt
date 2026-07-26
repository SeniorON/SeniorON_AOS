package com.example.senior_on.ui.common.seniorinfo

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private data class CaregiverRelationshipOption(
    val relationship: SeniorRelationship,
    @param:DrawableRes val iconResId: Int,
)

private val CaregiverRelationshipOptions = listOf(
    CaregiverRelationshipOption(
        relationship = SeniorRelationship.Mother,
        iconResId = R.drawable.ic_relationship_mother,
    ),
    CaregiverRelationshipOption(
        relationship = SeniorRelationship.Father,
        iconResId = R.drawable.ic_relationship_father,
    ),
    CaregiverRelationshipOption(
        relationship = SeniorRelationship.Grandparent,
        iconResId = R.drawable.ic_relationship_grandparent,
    ),
    CaregiverRelationshipOption(
        relationship = SeniorRelationship.Custom,
        iconResId = R.drawable.ic_pencil,
    ),
)

@Composable
fun CaregiverRelationshipInputScreen(
    seniorName: String,
    onBackClick: () -> Unit,
    onNextClick: (CaregiverRelationship) -> Unit,
    modifier: Modifier = Modifier,
    initialRelationship: SeniorRelationship? = null,
    initialCustomRelationship: String = "",
) {
    var selectedRelationship by rememberSaveable {
        mutableStateOf(initialRelationship)
    }
    var customRelationship by rememberSaveable {
        mutableStateOf(initialCustomRelationship)
    }
    var customRelationshipDraft by rememberSaveable {
        mutableStateOf(initialCustomRelationship)
    }
    var showCustomRelationshipSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val displayedRelationship = if (showCustomRelationshipSheet) {
        SeniorRelationship.Custom
    } else {
        selectedRelationship
    }
    val caregiverRelationship = when (selectedRelationship) {
        SeniorRelationship.Mother -> CaregiverRelationship(
            relation = SeniorRelationType.MOTHER,
        )
        SeniorRelationship.Father -> CaregiverRelationship(
            relation = SeniorRelationType.FATHER,
        )
        SeniorRelationship.Grandparent -> CaregiverRelationship(
            relation = SeniorRelationType.GRANDPARENT,
        )
        SeniorRelationship.Custom -> CaregiverRelationship(
            relation = SeniorRelationType.OTHER,
            customRelation = customRelationship.trim(),
        )
        null -> null
    }
    val isNextEnabled = caregiverRelationship
        ?.displayLabel
        ?.isNotBlank() == true

    if (showCustomRelationshipSheet) {
        CustomRelationshipBottomSheet(
            value = customRelationshipDraft,
            onValueChange = {
                customRelationshipDraft = it.take(CustomRelationshipMaxLength)
            },
            onCancel = {
                showCustomRelationshipSheet = false
            },
            onConfirm = {
                val trimmedRelationship = customRelationshipDraft.trim()

                if (trimmedRelationship.isNotEmpty()) {
                    customRelationship = trimmedRelationship
                        .take(CustomRelationshipMaxLength)
                    selectedRelationship = SeniorRelationship.Custom
                    showCustomRelationshipSheet = false
                }
            },
            confirmText = "저장",
            placeholder = "관계를 입력해주세요",
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        SeniorInfoTopBar(
            onBackClick = onBackClick,
            title = "정보 입력",
            backIconSize = 26.dp,
            showShadow = false,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(34.dp))
            CaregiverRelationshipTitle()
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "성함: $seniorName",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CaregiverRelationshipOptions.forEach { option ->
                    val isSelected = displayedRelationship == option.relationship
                    val customLabel = customRelationship.takeIf {
                        option.relationship == SeniorRelationship.Custom &&
                            it.isNotBlank()
                    }

                    CaregiverRelationshipItem(
                        label = customLabel ?: option.relationship.label,
                        iconResId = option.iconResId,
                        selected = isSelected,
                        showEditLabel = isSelected && customLabel != null,
                        onClick = {
                            if (option.relationship == SeniorRelationship.Custom) {
                                customRelationshipDraft = customRelationship
                                showCustomRelationshipSheet = true
                            } else {
                                customRelationship = ""
                                customRelationshipDraft = ""
                                selectedRelationship = option.relationship
                            }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SeniorOnColors.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 22.dp)
        ) {
            SeniorInfoActionButton(
                text = "다음",
                onClick = {
                    if (isNextEnabled) {
                        caregiverRelationship?.let(onNextClick)
                    }
                },
                style = SeniorInfoButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth(),
                enabled = isNextEnabled,
                height = 50.dp,
            )
        }
    }
}

@Composable
private fun CaregiverRelationshipTitle(
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                append("시니어")
            }
            append("와 어떤 관계이신가요?")
        },
        modifier = modifier,
        style = SeniorOnTextStyles.OnboardingHeading,
        color = SeniorOnColors.Gray800,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "나중에 설정에서 변경할 수 있어요.",
        style = SeniorOnTextStyles.BodySMedium,
        color = SeniorOnColors.Gray500,
    )
}

@Composable
private fun CaregiverRelationshipItem(
    label: String,
    @DrawableRes iconResId: Int,
    selected: Boolean,
    showEditLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(
                if (selected) {
                    SeniorOnColors.Primary200
                } else {
                    SeniorOnColors.Background3
                }
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = SeniorOnColors.Primary400,
                        shape = shape,
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
        )
        Text(
            text = label,
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800,
        )
        if (showEditLabel) {
            Text(
                text = "수정하기",
                modifier = Modifier.padding(end = 12.dp),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Primary600,
                textDecoration = TextDecoration.Underline,
            )
        }
        Icon(
            painter = painterResource(
                id = if (selected) {
                    R.drawable.ic_radio_button_1
                } else {
                    R.drawable.ic_radio_button_2
                }
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Primary600,
        )
    }
}

@Preview(
    name = "관계 미선택",
    showBackground = true,
    widthDp = 360,
    heightDp = 707,
)
@Composable
private fun CaregiverRelationshipInputScreenPreview() {
    SENIOR_ONTheme {
        CaregiverRelationshipInputScreen(
            seniorName = "김순자",
            onBackClick = {},
            onNextClick = {},
        )
    }
}

@Preview(
    name = "직접 작성 완료",
    showBackground = true,
    widthDp = 360,
    heightDp = 707,
)
@Composable
private fun CaregiverRelationshipCustomPreview() {
    SENIOR_ONTheme {
        CaregiverRelationshipInputScreen(
            seniorName = "김순자",
            onBackClick = {},
            onNextClick = {},
            initialRelationship = SeniorRelationship.Custom,
            initialCustomRelationship = "삼촌",
        )
    }
}
