package com.example.senior_on.ui.senior_info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

internal const val CustomRelationshipMaxLength = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomRelationshipBottomSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val isInPreview = LocalInspectionMode.current

    LaunchedEffect(isInPreview) {
        if (!isInPreview) {
            focusRequester.requestFocus()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.2f),
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp
        )
    ) {
        CustomRelationshipSheetContent(
            value = value,
            onValueChange = onValueChange,
            onCancel = onCancel,
            onConfirm = onConfirm,
            inputModifier = Modifier.focusRequester(focusRequester)
        )
    }
}

@Composable
internal fun CustomRelationshipSheetContent(
    value: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    inputModifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .height(154.dp)
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 30.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "취소",
                    modifier = Modifier
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .clickable(onClick = onCancel)
                        .padding(vertical = 10.dp),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "직접 작성",
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Gray800
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "확인",
                    modifier = Modifier
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 10.dp),
                    style = SeniorOnTextStyles.BodyMSemiBold,
                    color = SeniorOnColors.Primary600
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CustomRelationshipInputField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "관계(기타)",
            modifier = inputModifier
        )
    }
}

@Composable
private fun CustomRelationshipInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(43.dp),
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMRegular.copy(color = SeniorOnColors.Gray800),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        decorationBox = { innerTextField ->
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SeniorOnTextStyles.BodyMRegular,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SeniorOnColors.Gray200)
                )
            }
        }
    )
}
