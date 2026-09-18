package com.example.senior_on.ui.child.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.server.ServerConnectedSenior
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FamilyPhotoRecipientBottomSheet(
    seniors: List<ServerConnectedSenior>,
    selectedPhotoGroupIds: List<Long>,
    onPhotoGroupClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetryClick: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.42f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = SeniorOnRadius.XLarge,
            topEnd = SeniorOnRadius.XLarge,
        ),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Gray800),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "누구에게 공유할까요?",
                style = SeniorOnTextStyles.HeadingXS,
                color = SeniorOnColors.Gray800,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "여러 명을 함께 선택할 수 있어요",
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500,
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> RecipientLoadingContent()
                errorMessage != null -> RecipientErrorContent(
                    message = errorMessage,
                    onRetryClick = onRetryClick,
                )
                seniors.isEmpty() -> RecipientEmptyContent()
                else -> {
                    val visibleItemCount = seniors.size.coerceAtMost(3)
                    val listHeight = 80.dp * visibleItemCount.toFloat() +
                        8.dp * (visibleItemCount - 1).coerceAtLeast(0).toFloat()
                    LazyColumn(
                        modifier = Modifier.height(listHeight),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = seniors,
                            key = ServerConnectedSenior::photoGroupId,
                        ) { senior ->
                            FamilyPhotoRecipientItem(
                                senior = senior,
                                selected = senior.photoGroupId in selectedPhotoGroupIds,
                                onClick = { onPhotoGroupClick(senior.photoGroupId) },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FamilyTextActionButton(
                text = recipientShareButtonText(selectedPhotoGroupIds.size),
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.White,
                enabled = selectedPhotoGroupIds.isNotEmpty() && !isLoading && errorMessage == null,
                onClick = onShareClick,
                modifier = Modifier.fillMaxWidth(),
                buttonHeight = 44.dp,
            )
        }
    }
}

@Composable
private fun FamilyPhotoRecipientItem(
    senior: ServerConnectedSenior,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(shape)
            .background(
                if (selected) SeniorOnColors.Primary200 else SeniorOnColors.Background2,
            )
            .then(
                if (selected) {
                    Modifier.border(1.dp, SeniorOnColors.Primary400, shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SeniorOnColors.AccountAvatarBackground)
                .border(1.dp, SeniorOnColors.Green, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_senior_account_avatar),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = SeniorOnColors.AccountAvatarForeground,
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = senior.name,
                style = SeniorOnTextStyles.BodyLBold,
                color = SeniorOnColors.Gray800,
            )
            Text(
                text = senior.relationshipLabel,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray600,
            )
        }

        Icon(
            painter = painterResource(
                id = if (selected) {
                    R.drawable.ic_radio_button_1
                } else {
                    R.drawable.ic_radio_button_2
                },
            ),
            contentDescription = if (selected) "선택됨" else "선택 안 됨",
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Primary600,
        )
    }
}

@Composable
private fun RecipientLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = SeniorOnColors.Primary600,
            strokeWidth = 3.dp,
        )
    }
}

@Composable
private fun RecipientErrorContent(
    message: String,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "다시 시도",
            modifier = Modifier.clickable(onClick = onRetryClick),
            style = SeniorOnTextStyles.BodySSemiBold,
            color = SeniorOnColors.Primary600,
        )
    }
}

@Composable
private fun RecipientEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "연결된 시니어가 없어요",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
        )
    }
}

internal fun togglePhotoRecipientSelection(
    selectedPhotoGroupIds: List<Long>,
    photoGroupId: Long,
): List<Long> = if (photoGroupId in selectedPhotoGroupIds) {
    selectedPhotoGroupIds - photoGroupId
} else {
    selectedPhotoGroupIds + photoGroupId
}

internal fun recipientShareButtonText(selectedCount: Int): String =
    if (selectedCount == 0) "받을 분을 선택해 주세요" else "${selectedCount}명에게 공유하기"
