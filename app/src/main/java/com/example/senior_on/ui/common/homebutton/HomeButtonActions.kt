package com.example.senior_on.ui.common.homebutton

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.senior_on.domain.model.display.DisplayHomeButton

internal fun createDefaultHomeButtonIntent(actionValue: String): Intent? =
    when (actionValue.trim().uppercase()) {
        "PHONE", "CALL" -> Intent(Intent.ACTION_DIAL)
        "MESSAGE", "SMS" -> Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
        "CAMERA" -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        "PHOTO", "GALLERY" -> Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*",
            )
        }
        // 기존 저장 데이터 호환용. 신규 기본 기능 목록에는 노출하지 않는다.
        "CALENDAR" -> Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALENDAR)
        }
        "ALARM" -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
        "TIMER" -> Intent(AlarmClock.ACTION_SET_TIMER)
        "MEMO", "NOTE" -> Intent("android.intent.action.CREATE_NOTE")
        "VOICE_MEMO", "RECORDER", "VOICE_RECORDER" ->
            Intent("android.provider.MediaStore.RECORD_SOUND")
        "CALCULATOR" -> Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
        }
        "SETTINGS" -> Intent(Settings.ACTION_SETTINGS)
        "PLAY_STORE", "MARKET" -> Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://search?q=apps&c=apps"),
        ).apply { setPackage("com.android.vending") }
        "INTERNET", "BROWSER" -> Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com"),
        ).apply { addCategory(Intent.CATEGORY_BROWSABLE) }
        "MAP", "MAPS" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
        else -> null
    }

internal fun startDefaultHomeButtonAction(
    context: Context,
    actionValue: String,
): Boolean {
    val intent = createDefaultHomeButtonIntent(actionValue) ?: return false
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching { context.startActivity(intent) }.isSuccess
}

internal fun findMatchingDefaultButton(
    context: Context,
    packageName: String,
    defaultButtons: List<DisplayHomeButton>,
): DisplayHomeButton? {
    val normalizedPackage = packageName.trim()
    return defaultButtons.firstOrNull { button ->
        if (!button.actionType.equals("DEFAULT", ignoreCase = true)) {
            return@firstOrNull false
        }
        val intent = createDefaultHomeButtonIntent(button.actionValue)
            ?: return@firstOrNull false
        intent.setPackage(normalizedPackage)
        context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        ) != null
    }
}
