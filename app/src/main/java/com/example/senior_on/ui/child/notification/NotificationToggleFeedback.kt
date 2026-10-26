package com.example.senior_on.ui.child.notification

import com.example.senior_on.domain.model.auth.RemoteRequestException

internal const val HomeLocationMissingMessage = "등록된 자택 위치 정보가 없어요. 화면탭에서 자택 주소를 다시 검색해 저장해 주세요."

internal fun homeLocationToggleFeedback(category: NotificationCategory, failure: Exception): String? =
    HomeLocationMissingMessage.takeIf {
        category == NotificationCategory.Outing &&
            (failure as? RemoteRequestException)?.code == "SENIOR_HOME_LOCATION_NOT_FOUND"
    }
