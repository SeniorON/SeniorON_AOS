package com.example.senior_on.data.local

import android.content.Context

class FamilyPhotoSharePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE
    )

    fun shouldShowMessageTooltip(): Boolean =
        !preferences.getBoolean(MessageTooltipShownKey, false)

    fun markMessageTooltipShown() {
        preferences.edit()
            .putBoolean(MessageTooltipShownKey, true)
            .apply()
    }

    private companion object {
        const val PreferencesName = "family_photo_share_preferences"
        const val MessageTooltipShownKey = "message_tooltip_shown"
    }
}
