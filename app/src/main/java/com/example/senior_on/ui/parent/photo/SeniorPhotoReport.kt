@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.senior_on.ui.parent.photo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.senior_on.R
import com.example.senior_on.domain.model.family.FamilyPhotoReportReason
import com.example.senior_on.ui.common.component.SeniorOnActionButton
import com.example.senior_on.ui.parent.component.ParentDetailTopBar
import com.example.senior_on.ui.theme.*

internal enum class SeniorReportTarget(val title: String, val hideLabel: String, val icon: Int) {
    Photo("게시물 신고", "이 사진 다시 보지 않기", R.drawable.ic_family_report_post),
    Author("게시자 신고", "이 게시자의 게시물 보지 않기", R.drawable.ic_family_report_author),
}

@Composable
internal fun SeniorPhotoReportAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_big_report), null, Modifier.size(24.dp), tint = SeniorOnColors.Gray600)
        Spacer(Modifier.width(2.dp))
        Text("신고", style = SeniorOnTextStyles.BodyLMedium, color = SeniorOnColors.Gray600)
    }
}

@Composable
internal fun SeniorPhotoReportSheet(onDismiss: () -> Unit, onSelect: (SeniorReportTarget) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SeniorOnColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
    ) { TargetRows(onSelect) }
}

@Composable
private fun TargetRows(onSelect: (SeniorReportTarget) -> Unit) {
    Column(Modifier.fillMaxWidth().background(SeniorOnColors.White)
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp)) {
        Box(Modifier.align(Alignment.CenterHorizontally).size(32.dp, 4.dp)
            .background(SeniorOnColors.Gray800, RoundedCornerShape(2.dp)))
        Spacer(Modifier.height(44.dp))
        SeniorReportTarget.entries.forEachIndexed { index, target ->
            if (index > 0) HorizontalDivider(color = SeniorOnColors.Gray200)
            Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { onSelect(target) }
                .padding(horizontal = 12.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(target.icon), null, Modifier.size(34.dp), tint = SeniorOnColors.Gray800)
                Spacer(Modifier.width(10.dp))
                Text(target.title, Modifier.weight(1f), style = SeniorOnTextStyles.HeadingXL, color = SeniorOnColors.Gray800)
                Icon(painterResource(R.drawable.ic_sm_arrow_right), null, Modifier.size(38.dp), tint = SeniorOnColors.Gray500)
            }
        }
    }
}

private fun FamilyPhotoReportReason.label() = when (this) {
    FamilyPhotoReportReason.Advertisement -> "영리/홍보성"
    FamilyPhotoReportReason.Copyright -> "저작권 침해"
    FamilyPhotoReportReason.Sexual -> "음란/선정성"
    FamilyPhotoReportReason.Abuse -> "욕설/인신공격"
    FamilyPhotoReportReason.Privacy -> "개인정보 노출"
    FamilyPhotoReportReason.Duplicate -> "같은 사진 반복"
    FamilyPhotoReportReason.Etc -> "기타"
}

@Composable
internal fun SeniorPhotoReportForm(
    target: SeniorReportTarget,
    onBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    initialReason: FamilyPhotoReportReason? = null,
    sending: Boolean = false,
    accepted: Boolean = false,
    error: String? = null,
    onSubmit: (FamilyPhotoReportReason, String, Boolean) -> Unit = { _, _, _ -> },
) {
    var reason by rememberSaveable(target) { mutableStateOf(initialReason) }
    var detail by rememberSaveable(target) { mutableStateOf("") }
    var hide by rememberSaveable(target) { mutableStateOf(false) }
    val editable = !sending && !accepted
    val back = { if (editable) onBack() }
    BackHandler(onBack = back)
    Column(modifier.fillMaxSize().background(SeniorOnColors.White).safeDrawingPadding().clipToBounds().imePadding()) {
        ParentDetailTopBar(title = "${target.title} 사유", onBackClick = back)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Column(Modifier.selectableGroup()) {
                FamilyPhotoReportReason.entries.forEachIndexed { index, option ->
                    val selected = option == reason
                    if (index > 0) HorizontalDivider(color = SeniorOnColors.Gray200)
                    Row(Modifier.fillMaxWidth().selectable(selected, enabled = editable, role = Role.RadioButton) { reason = option }
                        .padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(if (selected) R.drawable.ic_radio_button_1 else R.drawable.ic_radio_button_2),
                            null, Modifier.size(30.dp), tint = if (selected) SeniorOnColors.Red300 else SeniorOnColors.Gray200)
                        Spacer(Modifier.width(14.dp))
                        Text(option.label(), style = SeniorOnTextStyles.HeadingS, color = SeniorOnColors.Gray800)
                    }
                }
            }
            if (reason == FamilyPhotoReportReason.Etc) {
                OutlinedTextField(detail, { detail = it.take(200) }, Modifier.fillMaxWidth().heightIn(min = 154.dp),
                    enabled = editable,
                    placeholder = { Text("신고 사유를 입력해 주세요", style = SeniorOnTextStyles.HeadingXXS) },
                    textStyle = SeniorOnTextStyles.HeadingXXS, shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SeniorOnColors.Gray600,
                        unfocusedBorderColor = SeniorOnColors.Gray200))
            }
            Row(Modifier.fillMaxWidth().toggleable(hide, enabled = editable, role = Role.Checkbox) { hide = it }
                .heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(if (hide) R.drawable.ic_check_filled else R.drawable.ic_check_unfilled), null,
                    Modifier.size(26.dp), tint = if (hide) SeniorOnColors.Red300 else SeniorOnColors.Gray300)
                Spacer(Modifier.width(8.dp))
                Text(target.hideLabel, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray800)
            }
            Text("•  신고는 반대 의견을 표시하는 기능이 아닙니다.\n•  허위 신고 또는 반복적인 악의적 신고는 제한될 수 있습니다.",
                Modifier.padding(horizontal = 8.dp), style = SeniorOnTextStyles.BodyLRegular, color = SeniorOnColors.Gray500)
        }
        if (error != null) Text(error, Modifier.padding(horizontal = 16.dp), color = SeniorOnColors.Red300)
        SeniorOnActionButton(text = if (sending) "접수 중…" else "신고하기", onClick = { reason?.let { onSubmit(it, detail, hide) } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), minHeight = 107.dp,
            enabled = editable && reason != null && (reason != FamilyPhotoReportReason.Etc || detail.isNotBlank()),
            textStyle = SeniorOnTextStyles.HeadingXL, containerColor = SeniorOnColors.Red300,
            shape = RoundedCornerShape(16.dp))
    }
    if (accepted) Dialog(onDismissRequest = {}) { Receipt(onFinished) }
}

@Composable
private fun Receipt(onConfirm: () -> Unit) {
    Column(Modifier.widthIn(max = 263.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(SeniorOnColors.White).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painterResource(R.drawable.ic_modal_check), null, Modifier.size(42.dp), tint = Color.Unspecified)
        Spacer(Modifier.height(12.dp))
        Text("신고가\n접수됐어요", style = SeniorOnTextStyles.HeadingXL, textAlign = TextAlign.Center, color = SeniorOnColors.Gray800)
        Spacer(Modifier.height(18.dp))
        Text("신고해 주신 내용은\n검토 후 운영 정책에 따라\n조치하겠습니다.", style = SeniorOnTextStyles.HeadingXS,
            textAlign = TextAlign.Center, color = SeniorOnColors.Gray500)
        Spacer(Modifier.height(26.dp))
        SeniorOnActionButton(text = "확인", onClick = onConfirm, modifier = Modifier.fillMaxWidth(),
            minHeight = 64.dp, textStyle = SeniorOnTextStyles.HeadingS, shape = RoundedCornerShape(12.dp))
    }
}

@Preview(name = "신고 대상", widthDp = 360, showBackground = true)
@Composable private fun TargetsPreview() { SENIOR_ONTheme { TargetRows {} } }

@Preview(name = "게시물 신고", widthDp = 360, heightDp = 886)
@Composable private fun PhotoFormPreview() { SENIOR_ONTheme { SeniorPhotoReportForm(SeniorReportTarget.Photo, {}, {}) } }

@Preview(name = "게시자 신고 · 기타", widthDp = 360, heightDp = 1040)
@Composable private fun AuthorFormPreview() { SENIOR_ONTheme { SeniorPhotoReportForm(SeniorReportTarget.Author, {}, {}, initialReason = FamilyPhotoReportReason.Etc) } }

@Preview(name = "신고 완료", widthDp = 263, showBackground = true)
@Composable private fun ReceiptPreview() { SENIOR_ONTheme { Receipt {} } }
