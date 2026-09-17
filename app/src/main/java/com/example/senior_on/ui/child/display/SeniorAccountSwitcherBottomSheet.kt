package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.parent.CaregiverRelationship
import com.example.senior_on.domain.model.parent.SeniorRelationType
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val SeniorAccountSheetShape = RoundedCornerShape(
    topStart = SeniorOnRadius.XLarge,
    topEnd = SeniorOnRadius.XLarge,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SeniorAccountSwitcherBottomSheet(
    accounts: List<ManagedSenior>,
    onDismiss: () -> Unit,
    onAccountClick: (ManagedSenior) -> Unit,
    onAddAccountClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SeniorOnColors.White,
        scrimColor = SeniorOnColors.Black.copy(alpha = 0.8f),
        tonalElevation = 0.dp,
        shape = SeniorAccountSheetShape,
        dragHandle = null,
    ) {
        SeniorAccountSwitcherSheetContent(
            accounts = accounts,
            onAccountClick = onAccountClick,
            onAddAccountClick = onAddAccountClick,
        )
    }
}

@Composable
private fun SeniorAccountSwitcherSheetContent(
    accounts: List<ManagedSenior>,
    onAccountClick: (ManagedSenior) -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        SeniorAccountSheetDragHandle()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            contentPadding = PaddingValues(bottom = 29.dp),
        ) {
            items(
                count = accounts.size,
                key = { index -> accounts[index].seniorId },
            ) { index ->
                val account = accounts[index]
                SeniorAccountOption(
                    account = account,
                    onClick = { onAccountClick(account) },
                )
                SeniorAccountDivider()
            }

            item(key = "add-senior-account") {
                AddSeniorAccountOption(onClick = onAddAccountClick)
            }
        }
    }
}

@Composable
private fun SeniorAccountSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(top = 28.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                .background(SeniorOnColors.Gray800),
        )
    }
}

@Composable
private fun SeniorAccountOption(
    account: ManagedSenior,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 67.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SeniorAccountAvatar()

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                style = SeniorOnTextStyles.BodyLSemiBold,
                color = SeniorOnColors.Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.relationship.displayLabel,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SeniorAccountAvatar() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SeniorOnColors.AccountAvatarBackground)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_senior_account_avatar),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = SeniorOnColors.AccountAvatarForeground,
        )
    }
}

@Composable
private fun SeniorAccountDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SeniorOnColors.Gray200),
    )
}

@Composable
private fun AddSeniorAccountOption(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SeniorOnColors.Gray200),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.display_add_senior_account),
            style = SeniorOnTextStyles.BodyLSemiBold,
            color = SeniorOnColors.Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(
    name = "Senior account switcher",
    showBackground = true,
    backgroundColor = 0xFF2C2C2C,
    widthDp = 360,
)
@Composable
private fun SeniorAccountSwitcherSheetPreview() {
    SENIOR_ONTheme {
        SeniorAccountSwitcherSheetContent(
            accounts = listOf(
                PreviewMotherAccount,
                PreviewFatherAccount,
            ),
            onAccountClick = {},
            onAddAccountClick = {},
            modifier = Modifier.background(SeniorOnColors.White),
        )
    }
}

private val PreviewMotherAccount = ManagedSenior(
    familyId = 1L,
    seniorId = 1L,
    parentUserId = 101L,
    name = "김순자",
    relationship = CaregiverRelationship(SeniorRelationType.MOTHER),
)

private val PreviewFatherAccount = ManagedSenior(
    familyId = 2L,
    seniorId = 2L,
    parentUserId = 102L,
    name = "박영훈",
    relationship = CaregiverRelationship(SeniorRelationType.FATHER),
)
