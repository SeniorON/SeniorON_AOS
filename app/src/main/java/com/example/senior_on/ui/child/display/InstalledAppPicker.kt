package com.example.senior_on.ui.child.display

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.ui.common.homebutton.findMatchingDefaultButton

internal data class PickedInstalledApp(
    val packageName: String,
    val label: String?,
)

internal val PickedInstalledApp.requiresManualNameInput: Boolean
    get() = label.isNullOrBlank()

internal fun PickedInstalledApp.supportedMusicButtonTypeOrNull():
    SeniorHomeButtonType? {
    SupportedMusicPackageTypes[packageName.trim().lowercase()]?.let { return it }

    val normalizedLabel = label.toMusicMatchKey()
    return SupportedMusicLabelAliases.firstNotNullOfOrNull { (type, aliases) ->
        type.takeIf { aliases.any(normalizedLabel::contains) }
    }
}

private val AndroidPackageNamePattern = Regex(
    pattern = "^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+$",
)

private fun createLaunchableAppIntent(): Intent {
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return launcherIntent
}

@Suppress("DEPRECATION")
internal fun loadLaunchableInstalledApps(context: Context): List<PickedInstalledApp> {
    val packageManager = context.packageManager
    val candidates = packageManager.queryIntentActivities(
        createLaunchableAppIntent(),
        PackageManager.MATCH_ALL,
    ).mapNotNull { resolveInfo ->
        val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
        val packageName = activityInfo.packageName
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: return@mapNotNull null
        val activityLabel = runCatching {
            resolveInfo.loadLabel(packageManager)?.toString()
        }.getOrNull()
        val applicationLabel = runCatching {
            packageManager.getApplicationLabel(activityInfo.applicationInfo).toString()
        }.getOrNull()

        PickedInstalledApp(
            packageName = packageName,
            label = selectInstalledAppDisplayName(
                packageName,
                activityLabel,
                applicationLabel,
            ),
        )
    }

    return normalizeLaunchableInstalledApps(
        candidates = candidates,
        excludedPackageName = context.packageName,
    )
}

internal fun normalizeLaunchableInstalledApps(
    candidates: List<PickedInstalledApp>,
    excludedPackageName: String,
): List<PickedInstalledApp> = candidates
    .filterNot { it.packageName.equals(excludedPackageName, ignoreCase = true) }
    .distinctBy { it.packageName.lowercase() }
    .sortedWith(
        compareBy<PickedInstalledApp> {
            (it.label ?: it.packageName).lowercase()
        }.thenBy { it.packageName.lowercase() },
    )

internal fun PickedInstalledApp.toDisplayHomeButton(
    context: Context,
    defaultButtons: List<DisplayHomeButton>,
    buttonName: String? = null,
): DisplayHomeButton? {
    val normalizedButtonName = buttonName
        ?.trim()
        ?.takeIf(String::isNotEmpty)
    supportedMusicButtonTypeOrNull()?.let { musicType ->
        return musicType.toImportedMusicButton(
            name = normalizedButtonName ?: label,
        )
    }
    val matchingDefaultButton = findMatchingDefaultButton(
        context = context,
        packageName = packageName,
        defaultButtons = defaultButtons,
    )

    if (matchingDefaultButton != null) {
        return normalizedButtonName
            ?.let { matchingDefaultButton.copy(name = it) }
            ?: matchingDefaultButton
    }

    val resolvedButtonName = normalizedButtonName
        ?: label?.trim()?.takeIf(String::isNotEmpty)
        ?: return null

    return DisplayHomeButton(
        name = resolvedButtonName,
        actionType = "APP",
        actionValue = packageName,
        packageName = packageName,
    )
}

private fun SeniorHomeButtonType.toImportedMusicButton(
    name: String?,
): DisplayHomeButton {
    val (defaultName, actionValue, packageName) = when (this) {
        SeniorHomeButtonType.Melon -> Triple("멜론", "MELON", "com.iloen.melon")
        SeniorHomeButtonType.Genie -> Triple("지니뮤직", "GENIE", "com.ktmusic.geniemusic")
        SeniorHomeButtonType.YouTubeMusic -> Triple(
            "유튜브뮤직",
            "YOUTUBE_MUSIC",
            "com.google.android.apps.youtube.music",
        )
        SeniorHomeButtonType.Spotify -> Triple(
            "스포티파이",
            "SPOTIFY",
            "com.spotify.music",
        )
        SeniorHomeButtonType.Flo -> Triple("플로", "FLO", "skplanet.musicmate")
        SeniorHomeButtonType.Vibe -> Triple("바이브", "VIBE", "com.naver.vibe")
        SeniorHomeButtonType.Bugs -> Triple("벅스", "BUGS", "com.neowiz.android.bugs")
        SeniorHomeButtonType.SamsungMusic -> Triple(
            "삼성뮤직",
            "SAMSUNG_MUSIC",
            "com.sec.android.app.music",
        )
        SeniorHomeButtonType.KakaoMusic -> Triple(
            "카카오뮤직",
            "KAKAO_MUSIC",
            "com.kakao.music",
        )
        else -> error("지원 음악 앱이 아닙니다: $this")
    }
    return DisplayHomeButton(
        name = name?.trim()?.takeIf(String::isNotEmpty) ?: defaultName,
        actionType = "APP",
        actionValue = actionValue,
        packageName = packageName,
        type = this,
    )
}

internal fun selectInstalledAppDisplayName(
    packageName: String,
    vararg candidates: String?,
): String? = candidates
    .asSequence()
    .mapNotNull { candidate -> candidate?.trim()?.takeIf(String::isNotEmpty) }
    .firstOrNull { candidate ->
        !candidate.equals(packageName, ignoreCase = true) &&
            !AndroidPackageNamePattern.matches(candidate)
    }

private fun String?.toMusicMatchKey(): String = orEmpty()
    .lowercase()
    .filter(Char::isLetterOrDigit)

private val SupportedMusicPackageTypes = mapOf(
    "com.iloen.melon" to SeniorHomeButtonType.Melon,
    "com.ktmusic.geniemusic" to SeniorHomeButtonType.Genie,
    "com.google.android.apps.youtube.music" to SeniorHomeButtonType.YouTubeMusic,
    "com.spotify.music" to SeniorHomeButtonType.Spotify,
    "skplanet.musicmate" to SeniorHomeButtonType.Flo,
    "com.naver.vibe" to SeniorHomeButtonType.Vibe,
    "com.neowiz.android.bugs" to SeniorHomeButtonType.Bugs,
    "com.sec.android.app.music" to SeniorHomeButtonType.SamsungMusic,
    "com.kakao.music" to SeniorHomeButtonType.KakaoMusic,
)

private val SupportedMusicLabelAliases = listOf(
    SeniorHomeButtonType.YouTubeMusic to listOf("유튜브뮤직", "youtubemusic"),
    SeniorHomeButtonType.SamsungMusic to listOf("삼성뮤직", "samsungmusic"),
    SeniorHomeButtonType.KakaoMusic to listOf("카카오뮤직", "kakaomusic"),
    SeniorHomeButtonType.Melon to listOf("멜론", "melon"),
    SeniorHomeButtonType.Genie to listOf("지니뮤직", "지니", "geniemusic", "genie"),
    SeniorHomeButtonType.Spotify to listOf("스포티파이", "spotify"),
    SeniorHomeButtonType.Flo to listOf("플로", "flo"),
    SeniorHomeButtonType.Vibe to listOf("바이브", "vibe"),
    SeniorHomeButtonType.Bugs to listOf("벅스", "bugs"),
)
