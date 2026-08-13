package com.example.senior_on.ui.common.homebutton

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.net.toUri
import com.example.senior_on.domain.model.display.DisplayHomeButton
import java.util.Locale

private const val LegacyCreateNoteAction = "com.google.android.gms.actions.CREATE_NOTE"
private const val CreateNoteAction = "android.intent.action.CREATE_NOTE"
private const val PlainTextMimeType = "text/plain"

internal fun createDefaultHomeButtonIntent(actionValue: String): Intent? =
    createDefaultHomeButtonIntents(actionValue).firstOrNull()

internal fun createDefaultHomeButtonIntents(actionValue: String): List<Intent> =
    when (actionValue.canonicalDefaultAction()) {
        "PHONE" -> listOf(Intent(Intent.ACTION_DIAL))
        "MESSAGE" -> listOf(
            createMainAppIntent(Intent.CATEGORY_APP_MESSAGING),
            Intent(Intent.ACTION_SENDTO, "smsto:".toUri()),
        )
        "CAMERA" -> listOf(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        "PHOTO" -> listOf(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*",
                )
            },
            createMainAppIntent(Intent.CATEGORY_APP_GALLERY),
        )
        // 기존 저장 데이터 호환용. 신규 기본 기능 목록에는 노출하지 않는다.
        "CALENDAR" -> listOf(
            createMainAppIntent(Intent.CATEGORY_APP_CALENDAR),
            Intent(
                Intent.ACTION_VIEW,
                "content://com.android.calendar/time".toUri(),
            ),
        )
        "ALARM" -> listOf(
            Intent(AlarmClock.ACTION_SHOW_ALARMS),
            Intent(AlarmClock.ACTION_SET_ALARM),
        )
        "TIMER" -> listOf(
            Intent(AlarmClock.ACTION_SHOW_TIMERS),
            Intent(AlarmClock.ACTION_SET_TIMER),
        )
        "MEMO" -> listOf(
            Intent(CreateNoteAction),
            Intent(LegacyCreateNoteAction).apply {
                type = PlainTextMimeType
            },
        )
        "VOICE_MEMO" -> listOf(
            Intent("android.provider.MediaStore.RECORD_SOUND"),
        )
        "CALCULATOR" -> listOf(
            createMainAppIntent(Intent.CATEGORY_APP_CALCULATOR),
        )
        "SETTINGS" -> listOf(Intent(Settings.ACTION_SETTINGS))
        "PLAY_STORE" -> listOf(
            createMainAppIntent(Intent.CATEGORY_APP_MARKET),
            Intent(
                Intent.ACTION_VIEW,
                "market://search?q=apps&c=apps".toUri(),
            ),
            Intent(
                Intent.ACTION_VIEW,
                "market://search?q=apps&c=apps".toUri(),
            ).apply { setPackage("com.android.vending") },
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps".toUri(),
            ),
        )
        "INTERNET" -> listOf(
            createMainAppIntent(Intent.CATEGORY_APP_BROWSER),
            Intent(
                Intent.ACTION_VIEW,
                "https://www.google.com".toUri(),
            ).apply { addCategory(Intent.CATEGORY_BROWSABLE) },
        )
        "MAP" -> listOf(
            Intent(Intent.ACTION_VIEW, "geo:0,0?q=".toUri()),
            createMainAppIntent(Intent.CATEGORY_APP_MAPS),
        )
        else -> emptyList()
    }

internal fun startDefaultHomeButtonAction(
    context: Context,
    actionValue: String,
): Boolean {
    val candidates = createDefaultHomeButtonIntents(actionValue)
    if (candidates.isEmpty()) return false

    val (resolvedCandidates, unresolvedCandidates) = candidates.partition { intent ->
        context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        ) != null
    }
    (resolvedCandidates + unresolvedCandidates).forEach { intent ->
        if (startActivity(context, intent)) return true
    }

    val labelFallback = findInstalledDefaultAppByLabel(context, actionValue)
        ?: return false
    return startActivity(context, labelFallback)
}

internal fun findMatchingDefaultButton(
    context: Context,
    packageName: String,
    defaultButtons: List<DisplayHomeButton>,
): DisplayHomeButton? {
    val normalizedPackage = packageName.trim()
    val packageManager = context.packageManager
    val applicationLabel = runCatching {
        val applicationInfo = packageManager.getApplicationInfo(normalizedPackage, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    }.getOrNull()

    return defaultButtons.firstOrNull { button ->
        if (!button.actionType.equals("DEFAULT", ignoreCase = true)) {
            return@firstOrNull false
        }
        val handlesDefaultIntent = createDefaultMatchingIntents(button.actionValue)
            .any { intent ->
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                    .any { resolveInfo ->
                        resolveInfo.activityInfo?.packageName.equals(
                            normalizedPackage,
                            ignoreCase = true,
                        )
                    }
            }
        handlesDefaultIntent || (
            applicationLabel != null &&
                matchesDefaultAppLabel(button.actionValue, applicationLabel)
            )
    }
}

internal fun matchesDefaultAppLabel(
    actionValue: String,
    applicationLabel: String,
): Boolean {
    val normalizedLabel = applicationLabel.normalizedAppLabel()
    return defaultAppLabelKeywords(actionValue).any { keyword ->
        normalizedLabel.contains(keyword.normalizedAppLabel())
    }
}

private fun createMainAppIntent(category: String): Intent =
    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category)

private fun createDefaultMatchingIntents(actionValue: String): List<Intent> =
    when (actionValue.canonicalDefaultAction()) {
        // 일반 https 딥링크 앱을 인터넷 기본앱으로 오인하지 않도록 브라우저 카테고리만 사용한다.
        "INTERNET" -> listOf(createMainAppIntent(Intent.CATEGORY_APP_BROWSER))
        // 이미지 뷰어 전체가 아니라 갤러리 역할을 가진 앱만 기본 사진 앱으로 판단한다.
        "PHOTO" -> listOf(createMainAppIntent(Intent.CATEGORY_APP_GALLERY))
        "MAP" -> listOf(createMainAppIntent(Intent.CATEGORY_APP_MAPS))
        // 웹 브라우저를 앱 마켓으로 오인하지 않도록 https fallback은 매칭에서 제외한다.
        "PLAY_STORE" -> createDefaultHomeButtonIntents(actionValue).dropLast(1)
        else -> createDefaultHomeButtonIntents(actionValue)
    }

private fun findInstalledDefaultAppByLabel(
    context: Context,
    actionValue: String,
): Intent? {
    val keywords = defaultAppLabelKeywords(actionValue)
    if (keywords.isEmpty()) return null

    val packageManager = context.packageManager
    val launcherQuery = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val matchedActivity = packageManager
        .queryIntentActivities(launcherQuery, PackageManager.MATCH_ALL)
        .asSequence()
        .mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val label = runCatching {
                resolveInfo.loadLabel(packageManager)?.toString().orEmpty()
            }.getOrDefault("")
            if (!matchesDefaultAppLabel(actionValue, label)) return@mapNotNull null
            val normalizedLabel = label.normalizedAppLabel()
            val matchScore = keywords.minOf { keyword ->
                val normalizedKeyword = keyword.normalizedAppLabel()
                when {
                    normalizedLabel == normalizedKeyword -> 0
                    normalizedLabel.startsWith(normalizedKeyword) -> 1
                    else -> 2
                }
            }
            Triple(matchScore, normalizedLabel, activityInfo)
        }
        .sortedWith(
            compareBy<Triple<Int, String, android.content.pm.ActivityInfo>>(
                { it.first },
                { it.second },
            ),
        )
        .firstOrNull()
        ?.third
        ?: return null

    return Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        component = ComponentName(
            matchedActivity.packageName,
            matchedActivity.name,
        )
    }
}

private fun startActivity(context: Context, source: Intent): Boolean {
    val intent = Intent(source).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching { context.startActivity(intent) }.isSuccess
}

private fun defaultAppLabelKeywords(actionValue: String): List<String> =
    when (actionValue.canonicalDefaultAction()) {
        "CALCULATOR" -> listOf("계산기", "calculator", "calc")
        "MEMO" -> listOf("메모", "노트", "memo", "notes", "note", "keep")
        "ALARM", "TIMER" -> listOf(
            "시계",
            "알람",
            "타이머",
            "clock",
            "alarm",
            "timer",
        )
        "INTERNET" -> listOf(
            "인터넷",
            "브라우저",
            "internet",
            "browser",
            "chrome",
        )
        "CALENDAR" -> listOf("캘린더", "달력", "calendar")
        "PHOTO" -> listOf("갤러리", "사진", "gallery", "photos")
        "VOICE_MEMO" -> listOf(
            "음성녹음",
            "녹음기",
            "voice recorder",
            "recorder",
        )
        else -> emptyList()
    }

private fun String.canonicalDefaultAction(): String = when (trim().uppercase()) {
    "CALL" -> "PHONE"
    "SMS" -> "MESSAGE"
    "GALLERY" -> "PHOTO"
    "NOTE" -> "MEMO"
    "RECORDER", "VOICE_RECORDER" -> "VOICE_MEMO"
    "MARKET" -> "PLAY_STORE"
    "BROWSER" -> "INTERNET"
    "MAPS" -> "MAP"
    else -> trim().uppercase()
}

private fun String.normalizedAppLabel(): String = lowercase(Locale.ROOT)
    .filterNot(Char::isWhitespace)
