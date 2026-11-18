package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.domain.model.senior.PendingSeniorFamily
import com.example.senior_on.ui.theme.SeniorOnColors

/** No senior-scoped route or request is mounted for a family without a profile. */
@Composable
internal fun PendingSeniorFamilyContent(
    family: PendingSeniorFamily,
    accounts: List<ManagedSenior>,
    pendingFamilies: List<PendingSeniorFamily>,
    onAccountClick: (ManagedSenior) -> Unit,
    onPendingClick: (PendingSeniorFamily) -> Unit,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onRestricted: () -> Unit,
    onDisconnect: () -> Unit,
    onRefresh: () -> Unit,
    refreshing: Boolean,
    busy: Boolean,
    modifier: Modifier = Modifier,
) {
    var showAccounts by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(SeniorOnColors.White)) {
        if (family.withdrawn) {
            DisplayTopBar(title = "탈퇴한 시니어", onAccountSelectorClick = { showAccounts = true })
            Text("탈퇴한 시니어", Modifier.fillMaxWidth().background(SeniorOnColors.Gray100).padding(24.dp))
            TextButton(onClick = onEdit, enabled = !busy) { Text("탈퇴 안내 확인") }
        } else DisplayTabScreen(
            uiState = DisplayTabUiState(relationshipLabel = "등록 중인 시니어"),
            modifier = Modifier.weight(1f),
            seniorAccounts = accounts,
            pendingFamilies = pendingFamilies,
            onSeniorAccountClick = onAccountClick,
            onPendingFamilyClick = onPendingClick,
            onAddSeniorAccountClick = onAdd,
            onParentInfoClick = { if (!busy) onEdit() },
            onDeviceClick = onRestricted,
            onFontEditClick = onRestricted,
            onButtonEditClick = onRestricted,
            onLargePreviewClick = onRestricted,
            isRefreshing = refreshing,
            onRefresh = onRefresh,
        )
        if (!family.withdrawn) TextButton(onClick = onDisconnect, enabled = !busy,
            modifier = Modifier.fillMaxWidth()) {
            Text(if (busy) "처리 중…" else "연결 해제", color = SeniorOnColors.Red300)
        }
    }
    if (showAccounts) SeniorAccountSwitcherBottomSheet(
        accounts = accounts, pendingFamilies = pendingFamilies,
        onDismiss = { showAccounts = false },
        onAccountClick = { showAccounts = false; onAccountClick(it) },
        onPendingFamilyClick = { showAccounts = false; onPendingClick(it) },
        onAddAccountClick = { showAccounts = false; onAdd() },
    )
}

@androidx.compose.ui.tooling.preview.Preview(name = "등록 중인 시니어", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PendingSeniorFamilyPreview() {
    val family = PendingSeniorFamily(1)
    com.example.senior_on.ui.theme.SENIOR_ONTheme {
        PendingSeniorFamilyContent(family, emptyList(), listOf(family), {}, {}, {}, {}, {}, {}, {}, false, false)
    }
}
