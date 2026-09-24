package com.example.senior_on.ui.parent.home

import android.content.Intent
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.senior_on.MainActivity
import com.example.senior_on.domain.model.display.SeniorHomeButtonType

@Composable
internal fun ParentSessionExpiredRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exitHome = rememberExitParentHomeAction()
    fun login() {
        context.startActivity(Intent(context, MainActivity::class.java))
        // A subsequent login creates fresh account-scoped launcher ViewModels.
        (context as? Activity)?.finish()
    }
    BackHandler { /* A launcher remains usable when Back is pressed. */ }
    ParentSessionExpiredScreen(
        now = rememberKoreaDateTime(),
        onLoginClick = ::login,
        onExitHomeClick = exitHome,
        onButtonClick = { type ->
            when (type) {
                SeniorHomeButtonType.Photo -> openSystemGallery(context)
                SeniorHomeButtonType.ChatBuddy, SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Schedule -> login()
                // Dialer only; never place an emergency call without user confirmation.
                SeniorHomeButtonType.Emergency -> openSeniorHomeButton(context, SeniorHomeButtonType.Call)
                else -> openSeniorHomeButton(context, type)
            }
        },
        modifier = modifier,
    )
}
