package com.example.senior_on.ui.parent.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.ui.parent.route.ParentLauncherRoute
import com.example.senior_on.ui.theme.SENIOR_ONTheme

class ParentLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                ParentLauncherRoute(
                    appContainer = (application as SeniorOnApplication).appContainer,
                )
            }
        }
    }

    /*
     * TODO(parent launcher mode):
     * 전용 런처 모드를 활성화할 때 별도 빌드 타입/Manifest에서 이 Activity에
     * ACTION_MAIN, CATEGORY_HOME, CATEGORY_DEFAULT intent-filter를 등록한다.
     * 현재는 MainActivity 안에서 일반 앱 화면으로 부모 모드를 실행한다.
     */
}
