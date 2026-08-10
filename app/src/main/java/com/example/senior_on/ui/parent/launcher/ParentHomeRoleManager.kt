package com.example.senior_on.ui.parent.launcher

import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

object ParentHomeRoleManager {
    fun enableHomeComponent(context: Context) {
        context.packageManager.setComponentEnabledSetting(
            homeComponent(context),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    fun isDefaultHome(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager
            .resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName == context.packageName
    }

    fun createHomeSelectionIntent(activity: Activity): Intent? {
        enableHomeComponent(activity)
        if (isDefaultHome(activity)) return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }

        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    private fun homeComponent(context: Context) = ComponentName(
        context,
        "${context.packageName}.ui.parent.launcher.ParentHomeAlias",
    )
}
