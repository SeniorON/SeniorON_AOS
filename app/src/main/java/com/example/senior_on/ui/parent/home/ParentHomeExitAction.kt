package com.example.senior_on.ui.parent.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.senior_on.R
import com.example.senior_on.ui.parent.launcher.ParentHomeRoleManager

/** Open the system-owned home picker; never guess another launcher's package name. */
@Composable
internal fun rememberExitParentHomeAction(): () -> Unit {
    val context = LocalContext.current
    var selectingHome by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        selectingHome = false
        // Settings has no meaningful resultCode: query the actual role after returning.
        if (ParentHomeRoleManager.isDefaultHome(context)) {
            ParentHomeRoleManager.setHomeExitRequested(context, false)
        } else {
            openSelectedHome(context)
        }
    }
    return action@{
        if (selectingHome) return@action
        ParentHomeRoleManager.setHomeExitRequested(context, true)
        if (!ParentHomeRoleManager.isDefaultHome(context)) {
            // Already using another default launcher (or the system resolver).
            openSelectedHome(context)
            return@action
        }
        selectingHome = true
        val opened = listOf(Settings.ACTION_HOME_SETTINGS, Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            .any { action -> runCatching { picker.launch(Intent(action)) }.isSuccess }
        if (opened) {
            Toast.makeText(context, R.string.parent_exit_home_select, Toast.LENGTH_LONG).show()
        } else {
            selectingHome = false
            ParentHomeRoleManager.setHomeExitRequested(context, false)
            Toast.makeText(context, R.string.parent_exit_home_failed, Toast.LENGTH_LONG).show()
        }
    }
}

private fun openSelectedHome(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        Toast.makeText(context, R.string.parent_exit_home_failed, Toast.LENGTH_LONG).show()
    }
}
