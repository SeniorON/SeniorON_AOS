package com.example.senior_on.ui.child.family

import com.example.senior_on.ui.common.share.ShareContent

internal fun familyInvitationShareContent(
    invitationCode: String,
): ShareContent = ShareContent(
    text = buildString {
        appendLine("SeniorON 가족으로 초대했어요.")
        appendLine("가족 공유 코드: $invitationCode")
        append("앱에서 코드를 입력해 가족을 연결해주세요.")
    },
    androidExecutionParams = mapOf(
        "familyCode" to invitationCode,
    ),
)
