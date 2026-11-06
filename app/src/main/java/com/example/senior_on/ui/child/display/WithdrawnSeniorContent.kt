package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import com.example.senior_on.ui.theme.SeniorOnRadius

/** No normal senior route is mounted while a deleted senior is selected. */
@Composable
internal fun WithdrawnSeniorContent(
    senior: ManagedSenior,
    accounts: List<ManagedSenior>,
    onAccountClick: (ManagedSenior) -> Unit,
    onConfirm: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAccounts by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(SeniorOnColors.White)) {
        DisplayTopBar(title = senior.name, onAccountSelectorClick = { showAccounts = true })
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(SeniorOnRadius.Large))
                    .background(SeniorOnColors.Gray100)
                    .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(SeniorOnRadius.Large))
                    .clickable(role = Role.Button, onClickLabel = "탈퇴 안내 확인", onClick = onConfirm)
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).background(SeniorOnColors.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_senior_account_avatar), null,
                            Modifier.size(34.dp), tint = SeniorOnColors.Gray400,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(senior.name, style = SeniorOnTextStyles.BodyLSemiBold, color = SeniorOnColors.Gray500)
                        Spacer(Modifier.height(4.dp))
                        Text("탈퇴한 시니어", style = SeniorOnTextStyles.BodySMedium, color = SeniorOnColors.Gray500)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "해당 시니어의 정보를 더 이상\n조회하거나 수정할 수 없어요.",
                    style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Gray500,
                )
                Spacer(Modifier.height(20.dp))
                Text("탈퇴 안내 확인", style = SeniorOnTextStyles.ButtonM, color = SeniorOnColors.Primary600)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "탈퇴 안내를 확인하면 내 시니어 목록에서 삭제돼요.",
                style = SeniorOnTextStyles.BodySMedium, color = SeniorOnColors.Gray500,
            )
        }
    }
    if (showAccounts) SeniorAccountSwitcherBottomSheet(
        accounts = accounts,
        onDismiss = { showAccounts = false },
        onAccountClick = { showAccounts = false; onAccountClick(it) },
        onAddAccountClick = { showAccounts = false; onAdd() },
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 320, heightDp = 480)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun WithdrawnSeniorContentPreview() {
    val senior = ManagedSenior(
        familyId = 1L, seniorId = 1L, parentUserId = null, name = "김순자",
        relationship = com.example.senior_on.domain.model.parent.CaregiverRelationship(
            com.example.senior_on.domain.model.parent.SeniorRelationType.MOTHER,
        ),
        withdrawn = true, withdrawalNoticeId = 1L,
    )
    com.example.senior_on.ui.theme.SENIOR_ONTheme {
        WithdrawnSeniorContent(senior, listOf(senior), {}, {}, {})
    }
}
