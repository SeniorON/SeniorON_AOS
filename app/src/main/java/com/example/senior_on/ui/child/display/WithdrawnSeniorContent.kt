package com.example.senior_on.ui.child.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.senior_on.domain.model.senior.ManagedSenior
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

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
    Column(modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
        TextButton(onClick = { showAccounts = true }) {
            Text("${senior.name} ▾", style = SeniorOnTextStyles.BodyLSemiBold)
        }
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth().background(SeniorOnColors.Gray100, RoundedCornerShape(16.dp))
                .clickable(onClick = onConfirm).padding(24.dp),
        ) {
            Text(senior.name, style = SeniorOnTextStyles.BodyLSemiBold, color = SeniorOnColors.Gray400)
            Spacer(Modifier.height(8.dp))
            Text("탈퇴한 시니어", color = SeniorOnColors.Gray500)
            Text("탈퇴 안내를 확인해 주세요.", color = SeniorOnColors.Gray500)
        }
    }
    if (showAccounts) SeniorAccountSwitcherBottomSheet(
        accounts = accounts,
        onDismiss = { showAccounts = false },
        onAccountClick = { showAccounts = false; onAccountClick(it) },
        onAddAccountClick = { showAccounts = false; onAdd() },
    )
}
