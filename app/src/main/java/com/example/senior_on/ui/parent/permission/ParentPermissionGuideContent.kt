package com.example.senior_on.ui.parent.permission

data class ParentPermissionGuideContent(
    val title: String,
    val emphasis: String,
    val description: String,
    val button: String = "설정하기",
    val mediaWidth: Int,
    val mediaHeight: Int,
) {
    val mediaAspectRatio: Float get() = mediaWidth.toFloat() / mediaHeight
}

/** Media dimensions are design-space dimensions, not fixed physical screen pixels. */
fun ParentPermissionStep.guideContent(): ParentPermissionGuideContent = when (this) {
    ParentPermissionStep.BatteryOptimization -> ParentPermissionGuideContent(
        "앱 정보에서 배터리를 누르고\n제한 없음으로 설정해 주세요.", "제한 없음",
        "시니어가 앱을 사용 중이지 않을 때에도\n시니어On이 동작하기 위해 해당 권한이 필요해요.",
        mediaWidth = 279, mediaHeight = 182,
    )
    ParentPermissionStep.Notification -> ParentPermissionGuideContent(
        "알림 권한을\n허용으로 설정해 주세요.", "허용",
        "복약 시간과 병원 일정 알림 등 중요 알림을 놓치지\n않으려면 해당 권한이 필요해요.",
        mediaWidth = 260, mediaHeight = 202,
    )
    ParentPermissionStep.ForegroundLocation -> ParentPermissionGuideContent(
        "위치 권한을 앱 사용중에만 허용으로 설정해 주세요.", "앱 사용중에만 허용",
        "긴급 상황이나 외출,귀가 시\n시니어의 위치를 확인하기 위해 필요한 권한이에요.",
        mediaWidth = 262, mediaHeight = 205,
    )
    ParentPermissionStep.BackgroundLocation -> ParentPermissionGuideContent(
        "백그라운드 위치 엑세스 권한을\n항상 허용으로 설정해 주세요.", "항상 허용",
        "앱을 사용하지 않는 중에도 긴급 상황에서\n시니어의 위치를 확인할 수 있도록 필요한 권한이에요.\n백그라운드 위치 엑세스 권한 사용에 동의하시면\n위치 권한을 ‘항상 허용’으로 변경해 주세요.",
        button = "동의하고 설정하기", mediaWidth = 262, mediaHeight = 230,
    )
    ParentPermissionStep.DefaultHome -> ParentPermissionGuideContent(
        "기본 홈 화면을\n시니어On으로 설정해 주세요.", "시니어On",
        "시니어On으로 구성한 홈 화면을 휴대폰의\n기본 화면으로 사용하기 위해 필요한 권한이에요.",
        mediaWidth = 267, mediaHeight = 230,
    )
    ParentPermissionStep.SleepingApps -> ParentPermissionGuideContent(
        "절전 앱 관리에서\n시니어On을 제외해 주세요.", "시니어On을 제외",
        "삼성 기기의 절전 기능으로 인해 시니어On의\n백그라운드 동작이 제한될 수 있어요.\n터치 및 위치 확인 등 중요한 기능이 원활하게 작동하도록 절전 앱에서 제외해 주세요.",
        mediaWidth = 267, mediaHeight = 217,
    )
}
