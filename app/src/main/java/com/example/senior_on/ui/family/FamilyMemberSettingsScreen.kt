package com.example.senior_on.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.data.model.MockFamilyFixtures
import com.example.senior_on.ui.child.ChildBottomNavigation
import com.example.senior_on.ui.child.ChildMainTab
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

private val ScreenHorizontalPadding = 16.dp
private val MemberAvatarSize = 48.dp
private val BottomActionHeight = 48.dp

@Composable
fun FamilyMemberSettingsScreen(
    uiState: FamilyTabUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddFamilyClick: () -> Unit = {},
    onChangePrimaryClick: (String) -> Unit = {},
    onDeleteMemberClick: (String) -> Unit = {}
) {
    val primaryMember = uiState.members.firstOrNull { member ->
        member.role == FamilyCaregiverRole.Primary
    }
    val assistantMembers = uiState.members.filter { member ->
        member.role == FamilyCaregiverRole.Assistant
    }
    var selectedMemberId by rememberSaveable { mutableStateOf<String?>(null) }
    var isPermissionGuideExpanded by rememberSaveable { mutableStateOf(false) }
    var isChangePrimaryDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDeleteMemberDialogVisible by rememberSaveable { mutableStateOf(false) }
    val selectedMember = assistantMembers.firstOrNull { member ->
        member.id == selectedMemberId
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
    ) {
        FamilyBackTopAppBar(
            title = "구성원 설정",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(SeniorOnColors.Background1)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = ScreenHorizontalPadding,
                    top = 24.dp,
                    end = ScreenHorizontalPadding,
                    bottom = 16.dp
                )
            ) {
                item {
                    FamilyMemberSettingsHeader()

                    Spacer(modifier = Modifier.height(12.dp))

                    primaryMember?.let { member ->
                        PrimaryMemberCard(member = member)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (assistantMembers.isEmpty()) {
                        EmptyAssistantMemberCard()
                    } else {
                        AssistantMemberSectionTitle()

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            assistantMembers.forEach { member ->
                                AssistantMemberCard(
                                    member = member,
                                    selected = selectedMemberId == member.id,
                                    onClick = { selectedMemberId = member.id }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FamilyPermissionGuide(
                        expanded = isPermissionGuideExpanded,
                        onClick = {
                            isPermissionGuideExpanded = !isPermissionGuideExpanded
                        }
                    )
                }
            }

            FamilyMemberSettingsActions(
                hasAssistantMember = assistantMembers.isNotEmpty(),
                selectedMemberId = selectedMember?.id,
                isOperationInProgress = uiState.isMemberMutationInProgress,
                errorMessage = uiState.memberMutationErrorMessage,
                onAddFamilyClick = onAddFamilyClick,
                onChangePrimaryRequest = {
                    isChangePrimaryDialogVisible = true
                },
                onDeleteMemberRequest = {
                    isDeleteMemberDialogVisible = true
                }
            )
        }
    }

    selectedMember?.let { member ->
        if (isChangePrimaryDialogVisible) {
            ChangePrimaryConfirmationDialog(
                memberName = member.name,
                onDismiss = { isChangePrimaryDialogVisible = false },
                onConfirm = {
                    isChangePrimaryDialogVisible = false
                    onChangePrimaryClick(member.id)
                }
            )
        }

        if (isDeleteMemberDialogVisible) {
            DeleteMemberConfirmationDialog(
                memberName = member.name,
                onDismiss = { isDeleteMemberDialogVisible = false },
                onConfirm = {
                    isDeleteMemberDialogVisible = false
                    onDeleteMemberClick(member.id)
                }
            )
        }
    }
}

@Composable
private fun FamilyMemberSettingsHeader() {
    Text(
        text = "구성원 목록",
        style = SeniorOnTextStyles.HeadingS,
        color = SeniorOnColors.Gray800
    )

    Spacer(modifier = Modifier.height(2.dp))

    Text(
        text = "구성원 권한을 변경하거나 삭제할 수 있어요",
        style = SeniorOnTextStyles.BodySMedium,
        color = SeniorOnColors.Gray500
    )
}

@Composable
private fun PrimaryMemberCard(member: FamilyMemberUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsMemberImage(member = member)

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = member.name,
            modifier = Modifier.weight(1f),
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800
        )

        Text(
            text = member.role.label,
            modifier = Modifier
                .clip(RoundedCornerShape(17.dp))
                .background(SeniorOnColors.Primary200)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Primary600
        )
    }
}

@Composable
private fun AssistantMemberSectionTitle() {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SeniorOnTextStyles.BodySSemiBold.toSpanStyle().copy(
                    color = SeniorOnColors.Primary700
                )
            ) {
                append("변경할 구성원을")
            }
            withStyle(
                SeniorOnTextStyles.BodySMedium.toSpanStyle().copy(
                    color = SeniorOnColors.Gray500
                )
            ) {
                append(" 선택하세요")
            }
        },
        style = SeniorOnTextStyles.BodySMedium
    )
}

@Composable
private fun AssistantMemberCard(
    member: FamilyMemberUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Medium)
    val backgroundColor = if (selected) {
        SeniorOnColors.Primary200
    } else {
        SeniorOnColors.Background1
    }
    val borderColor = if (selected) {
        SeniorOnColors.Primary400
    } else {
        SeniorOnColors.Gray200
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsMemberImage(member = member)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = member.role.label,
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500
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
            tint = SeniorOnColors.Primary600
        )
    }
}

@Composable
private fun SettingsMemberImage(member: FamilyMemberUiModel) {
    Box(
        modifier = Modifier
            .size(MemberAvatarSize)
            .clip(CircleShape)
            .background(SeniorOnColors.Background4)
    ) {
        FamilyMemberImage(member)
    }
}

@Composable
private fun EmptyAssistantMemberCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(169.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray200,
                shape = RoundedCornerShape(SeniorOnRadius.Medium)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_empty_family_members),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "다른 구성원이 없어요",
            style = SeniorOnTextStyles.BodyMSemiBold,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "구성원을 추가하면 주 담당자를\n변경할 수 있어요.",
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Gray500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FamilyPermissionGuide(
    expanded: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SeniorOnColors.Background1)
            .border(1.dp, SeniorOnColors.Gray200, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(onClick = onClick)
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_information),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "권한은 이렇게 나뉘어요",
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodySMedium,
                color = SeniorOnColors.Gray500
            )

            Icon(
                painter = painterResource(
                    id = if (expanded) {
                        R.drawable.ic_sm_fold
                    } else {
                        R.drawable.ic_sm_chevron_down_2
                    }
                ),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = SeniorOnColors.Gray500
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier.padding(
                    start = 10.dp,
                    top = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                )
            ) {
                PermissionRoleDescription(
                    iconResId = R.drawable.ic_dependent,
                    title = "주 담당자",
                    description = "화면 편집·구성원/권한 관리·모든 기능"
                )

                Spacer(modifier = Modifier.height(12.dp))

                PermissionRoleDescription(
                    iconResId = R.drawable.ic_dependents,
                    title = "보조 담당자",
                    description = "알림 받기·기록 보기·사진 공유"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = SeniorOnColors.Gray200
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sm_add_circle),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SeniorOnColors.Gray500
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "주 담당자는 보조 담당자 권한을 모두 포함해요",
                        style = SeniorOnTextStyles.CaptionMedium,
                        color = SeniorOnColors.Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRoleDescription(
    iconResId: Int,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = SeniorOnColors.Primary600
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = SeniorOnTextStyles.BodySSemiBold,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = description,
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
private fun FamilyMemberSettingsActions(
    hasAssistantMember: Boolean,
    selectedMemberId: String?,
    isOperationInProgress: Boolean,
    errorMessage: String?,
    onAddFamilyClick: () -> Unit,
    onChangePrimaryRequest: () -> Unit,
    onDeleteMemberRequest: () -> Unit
) {
    val isMemberSelected = selectedMemberId != null && !isOperationInProgress

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SeniorOnColors.Background1)
            .padding(
                start = ScreenHorizontalPadding,
                top = 12.dp,
                end = ScreenHorizontalPadding,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        errorMessage?.let {
            Text(
                text = it,
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Red300,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (hasAssistantMember) {
            FamilyActionButton(
                text = "주 담당자로 변경",
                iconResId = R.drawable.ic_change,
                enabled = isMemberSelected,
                onClick = onChangePrimaryRequest,
                buttonHeight = BottomActionHeight,
                iconSpacing = 6.dp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .height(48.dp)
                    .alpha(if (isMemberSelected) 1f else 0.5f)
                    .clickable(
                        enabled = isMemberSelected,
                        onClick = onDeleteMemberRequest
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Red300
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "구성원 삭제",
                    style = SeniorOnTextStyles.ButtonM,
                    color = SeniorOnColors.Red300
                )
            }
        } else {
            FamilyActionButton(
                text = "가족 추가하기",
                iconResId = R.drawable.ic_plus,
                onClick = onAddFamilyClick,
                buttonHeight = BottomActionHeight,
                iconSpacing = 6.dp,
            )
        }
    }
}

@Composable
private fun DeleteMemberConfirmationDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showAsDialog: Boolean = true
) {
    FamilyConfirmDialog(
        iconResId = R.drawable.ic_modal_trash,
        dialogHeight = 189.dp,
        verticalPadding = 28.dp,
        title = "${memberName.withObjectParticle()} 삭제할까요?",
        confirmText = "삭제하기",
        confirmBackgroundColor = SeniorOnColors.Red400,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        showAsDialog = showAsDialog
    )
}

@Composable
private fun ChangePrimaryConfirmationDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    showAsDialog: Boolean = true
) {
    FamilyConfirmDialog(
        iconResId = R.drawable.ic_modal_change,
        dialogHeight = 262.dp,
        verticalPadding = 24.dp,
        title = "${memberName.withObjectParticle()} 주 담당자로\n변경할까요?",
        description = "변경하면 현재 주 담당자인 회원님은\n보조 담당자로 바뀌어요",
        confirmText = "변경하기",
        confirmBackgroundColor = SeniorOnColors.Primary600,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        showAsDialog = showAsDialog
    )
}

private fun String.withObjectParticle(): String {
    val lastCharacter = lastOrNull() ?: return this
    val hasFinalConsonant = lastCharacter in '가'..'힣' &&
        (lastCharacter.code - '가'.code) % 28 != 0
    return this + if (hasFinalConsonant) "을" else "를"
}

@Preview(
    name = "Member Settings - Members",
    showBackground = true,
    widthDp = 360,
    heightDp = 888
)
@Composable
private fun FamilyMemberSettingsMembersPreview() {
    FamilyMemberSettingsPreviewFrame(
        uiState = MockFamilyFixtures.primaryCaregiverOverview.toFamilyTabUiState()
    )
}

@Preview(
    name = "Member Settings - Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 888
)
@Composable
private fun FamilyMemberSettingsEmptyPreview() {
    val uiState = MockFamilyFixtures.primaryCaregiverOverview
        .toFamilyTabUiState()
        .let { state ->
            state.copy(
                members = state.members.filter { member ->
                    member.role == FamilyCaregiverRole.Primary
                }
            )
        }

    FamilyMemberSettingsPreviewFrame(uiState = uiState)
}

@Composable
private fun FamilyMemberSettingsPreviewFrame(uiState: FamilyTabUiState) {

    SENIOR_ONTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background2)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FamilyMemberSettingsScreen(
                    uiState = uiState,
                    onBackClick = {}
                )
            }

            ChildBottomNavigation(
                selectedTab = ChildMainTab.Family,
                onTabClick = {}
            )
        }
    }
}

@Preview(
    name = "Member Settings - Delete Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 320
)
@Composable
private fun DeleteMemberConfirmationDialogPreview() {
    val memberName = previewAssistantMemberName()

    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background1),
            contentAlignment = Alignment.Center
        ) {
            DeleteMemberConfirmationDialog(
                memberName = memberName,
                onDismiss = {},
                onConfirm = {},
                showAsDialog = false
            )
        }
    }
}

@Preview(
    name = "Member Settings - Change Primary Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 360
)
@Composable
private fun ChangePrimaryConfirmationDialogPreview() {
    val memberName = previewAssistantMemberName()

    SENIOR_ONTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Background1),
            contentAlignment = Alignment.Center
        ) {
            ChangePrimaryConfirmationDialog(
                memberName = memberName,
                onDismiss = {},
                onConfirm = {},
                showAsDialog = false
            )
        }
    }
}

private fun previewAssistantMemberName(): String =
    MockFamilyFixtures.primaryCaregiverOverview
        .toFamilyTabUiState()
        .members
        .first { member -> member.role == FamilyCaregiverRole.Assistant }
        .name
