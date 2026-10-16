package com.example.senior_on.ui.onboarding.viewmodel

import com.example.senior_on.domain.model.auth.RemoteRequestException
import java.io.IOException

internal fun recoveryVerificationErrorText(error: Throwable): String =
    if ((error as? RemoteRequestException)?.code == "USER400_3") {
        "인증번호가 올바르지 않거나 만료됐어요. 다시 확인하거나 재전송해 주세요."
    } else recoveryLookupErrorText(error, passwordRecovery = true)

internal fun recoveryLookupErrorText(error: Throwable, passwordRecovery: Boolean): String {
    val remote = error as? RemoteRequestException
    return when {
        remote?.code in setOf("USER404_1", "USER404_2") -> if (passwordRecovery) {
            "이름과 아이디가 일치하는 계정을 찾지 못했어요. 입력한 정보를 확인해 주세요."
        } else {
            "이름과 이메일이 일치하는 계정을 찾지 못했어요. 입력한 정보를 확인해 주세요."
        }
        remote?.status == 429 -> "요청이 많아요. 잠시 후 다시 시도해 주세요."
        remote?.code == "EMAIL500" -> "인증번호를 보내지 못했어요. 잠시 후 다시 시도해 주세요."
        error is IOException -> "네트워크 연결을 확인한 후 다시 시도해 주세요."
        else -> "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요."
    }
}
