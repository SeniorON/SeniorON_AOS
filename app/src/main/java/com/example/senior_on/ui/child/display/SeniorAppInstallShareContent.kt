package com.example.senior_on.ui.child.display

import com.example.senior_on.ui.common.share.ShareContent

internal fun seniorAppInstallShareContent(): ShareContent = ShareContent(
    text = """
        [시니어ON 앱 설치 및 연결 안내]

        1. 휴대폰에서 Play 스토어를 열어 주세요.
        2. '시니어ON'을 검색해 앱을 설치해 주세요.
        3. 앱을 실행한 뒤 '시니어'를 선택하고 회원가입해 주세요.
        4. 가족 코드 입력 화면에서 보호자에게 전달받은 코드를 입력해 주세요.

        가족 코드 입력을 마치면 보호자와 연결됩니다.
    """.trimIndent(),
)
