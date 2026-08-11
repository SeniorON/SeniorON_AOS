package com.example.senior_on.ui.parent.launcher

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Settings

object ParentBrowserRoleManager {
    fun createBrowserSelectionIntent(activity: Activity): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_BROWSER) == true) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) return null
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
            }
        }

        return Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    }
}
