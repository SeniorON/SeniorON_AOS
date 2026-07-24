package com.example.senior_on.ui.health

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.repository.MockHospitalSpecialtyRepository
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private const val CollapsedSpecialtyCount = 9
private const val DirectInputOption = "직접 작성"

@Composable
internal fun HospitalSpecialtyBottomSheet(
    specialties: List<String>,
    selected: String,
    onCancel: () -> Unit,
    onDirectInput: () -> Unit,
    onSave: (String) -> Unit
) {
    var pending by rememberSaveable(selected, specialties) {
        mutableStateOf(
            when {
                selected.isEmpty() -> ""
                specialties.contains(selected) -> selected
                else -> DirectInputOption
            }
        )
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val visibleSpecialties = if (expanded) specialties else specialties.take(CollapsedSpecialtyCount)
    val canSave = pending.isNotEmpty() && pending != DirectInputOption
    NonDraggableBottomSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "진료 과목",
                style = SeniorOnTextStyles.BodyLMedium,
                color = SeniorOnColors.Gray800
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 250))
            ) {
                visibleSpecialties.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { specialty ->
                            SpecialtyOption(
                                label = specialty,
                                selected = specialty == pending,
                                onClick = {
                                    if (specialty == DirectInputOption) {
                                        onDirectInput()
                                    } else {
                                        pending = specialty
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (expanded) R.drawable.ic_sm_fold
                        else R.drawable.ic_sm_chevron_down_2
                    ),
                    contentDescription = null,
                    tint = SeniorOnColors.Primary600,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (expanded) "접기" else "더보기",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Primary600
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecialtySheetButton(
                    label = "취소",
                    backgroundColor = SeniorOnColors.SupportWhite100,
                    contentColor = SeniorOnColors.Gray400,
                    borderColor = SeniorOnColors.Gray200,
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                SpecialtySheetButton(
                    label = "저장",
                    backgroundColor = SeniorOnColors.Primary600,
                    contentColor = SeniorOnColors.SupportWhite100,
                    enabled = canSave,
                    onClick = { onSave(pending) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SpecialtyOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    Box(
        modifier = modifier
            .height(49.dp)
            .clip(shape)
            .background(
                if (selected) SeniorOnColors.Primary100
                else SeniorOnColors.SupportWhite100
            )
            .border(
                1.dp,
                if (selected) SeniorOnColors.Primary400 else SeniorOnColors.Gray200,
                shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = SeniorOnTextStyles.BodySMedium,
            color = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun SpecialtySheetButton(
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    val resolvedBackgroundColor = if (enabled) {
        backgroundColor
    } else {
        backgroundColor.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(resolvedBackgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = SeniorOnTextStyles.ButtonM, color = contentColor)
    }
}

@Preview(
    name = "Hospital Specialty Bottom Sheet",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun HospitalSpecialtyBottomSheetPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(SeniorOnColors.SupportWhite100)
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "진료 과목",
                    style = SeniorOnTextStyles.BodyLMedium,
                    color = SeniorOnColors.Gray800
                )
                Spacer(modifier = Modifier.height(20.dp))
                MockHospitalSpecialtyRepository.specialties
                    .take(CollapsedSpecialtyCount)
                    .chunked(3)
                    .forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { specialty ->
                                SpecialtyOption(
                                    label = specialty,
                                    selected = specialty == "내과",
                                    onClick = {},
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sm_chevron_down_2),
                        contentDescription = null,
                        tint = SeniorOnColors.Primary600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "더보기",
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Primary600
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecialtySheetButton(
                        label = "취소",
                        backgroundColor = SeniorOnColors.SupportWhite100,
                        contentColor = SeniorOnColors.Gray400,
                        borderColor = SeniorOnColors.Gray200,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    SpecialtySheetButton(
                        label = "저장",
                        backgroundColor = SeniorOnColors.Primary600,
                        contentColor = SeniorOnColors.SupportWhite100,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Hospital Specialty Bottom Sheet - Expanded",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun HospitalSpecialtyBottomSheetExpandedPreview() {
    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .background(SeniorOnColors.SupportWhite100)
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "진료 과목",
                    style = SeniorOnTextStyles.BodyLMedium,
                    color = SeniorOnColors.Gray800
                )
                Spacer(modifier = Modifier.height(20.dp))
                MockHospitalSpecialtyRepository.specialties
                    .chunked(3)
                    .forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { specialty ->
                                SpecialtyOption(
                                    label = specialty,
                                    selected = specialty == "내과",
                                    onClick = {},
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sm_fold),
                        contentDescription = null,
                        tint = SeniorOnColors.Primary600,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "접기",
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Primary600
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecialtySheetButton(
                        label = "취소",
                        backgroundColor = SeniorOnColors.SupportWhite100,
                        contentColor = SeniorOnColors.Gray400,
                        borderColor = SeniorOnColors.Gray200,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                    SpecialtySheetButton(
                        label = "저장",
                        backgroundColor = SeniorOnColors.Primary600,
                        contentColor = SeniorOnColors.SupportWhite100,
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
