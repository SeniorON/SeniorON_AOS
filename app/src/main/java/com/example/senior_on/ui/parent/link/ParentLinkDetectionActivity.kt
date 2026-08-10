package com.example.senior_on.ui.parent.link

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.senior_on.SeniorOnApplication
import com.example.senior_on.data.remote.dto.UserRole
import com.example.senior_on.data.source.auth.PersistedSessionStore
import com.example.senior_on.ui.parent.home.openExternalBrowser
import com.example.senior_on.ui.theme.SENIOR_ONTheme

class ParentLinkDetectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.data?.takeIf(Uri::isSupportedWebUrl)?.toString()
        val isParentSession =
            PersistedSessionStore(this).getSession()?.role == UserRole.PARENT
        if (url == null || !isParentSession) {
            url?.let { openExternalBrowser(this, it) }
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            SENIOR_ONTheme {
                ParentLinkDetectionRoute(
                    repository =
                        (application as SeniorOnApplication).appContainer.parentLinkSafetyRepository,
                    url = url,
                    onBackClick = ::finish,
                )
            }
        }
    }
}

private fun Uri.isSupportedWebUrl(): Boolean =
    scheme.equals("http", ignoreCase = true) ||
        scheme.equals("https", ignoreCase = true)
