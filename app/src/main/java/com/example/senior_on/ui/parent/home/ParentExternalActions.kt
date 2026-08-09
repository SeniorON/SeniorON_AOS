package com.example.senior_on.ui.parent.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.example.senior_on.domain.model.display.SeniorHomeButtonType

internal fun openSeniorHomeButton(
    context: Context,
    button: SeniorHomeButtonType,
) {
    when (button) {
        SeniorHomeButtonType.Call ->
            startIntent(context, Intent(Intent.ACTION_DIAL))
        SeniorHomeButtonType.Message ->
            startIntent(context, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")))
        SeniorHomeButtonType.Camera ->
            startIntent(context, Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        SeniorHomeButtonType.Calendar -> startIntent(
            context,
            Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time")),
        )
        SeniorHomeButtonType.Settings ->
            startIntent(context, Intent(Settings.ACTION_SETTINGS))
        SeniorHomeButtonType.YouTube ->
            openAppOrPlayStore(context, "com.google.android.youtube")
        SeniorHomeButtonType.Melon ->
            openAppOrPlayStore(context, "com.iloen.melon")
        SeniorHomeButtonType.Genie ->
            openAppOrPlayStore(context, "com.ktmusic.geniemusic")
        SeniorHomeButtonType.YouTubeMusic ->
            openAppOrPlayStore(context, "com.google.android.apps.youtube.music")
        SeniorHomeButtonType.Spotify ->
            openAppOrPlayStore(context, "com.spotify.music")
        SeniorHomeButtonType.Flo ->
            openAppOrPlayStore(context, "skplanet.musicmate")
        SeniorHomeButtonType.Vibe ->
            openAppOrPlayStore(context, "com.naver.vibe")
        SeniorHomeButtonType.Bugs ->
            openAppOrPlayStore(context, "com.neowiz.android.bugs")
        SeniorHomeButtonType.SamsungMusic ->
            openAppOrPlayStore(context, "com.sec.android.app.music")
        SeniorHomeButtonType.KakaoMusic ->
            openAppOrPlayStore(context, "com.kakao.music")
        SeniorHomeButtonType.NaverMap ->
            openAppOrPlayStore(context, "com.nhn.android.nmap")
        SeniorHomeButtonType.KakaoMap ->
            openAppOrPlayStore(context, "net.daum.android.map")
        SeniorHomeButtonType.KakaoTalk ->
            openAppOrPlayStore(context, "com.kakao.talk")
        SeniorHomeButtonType.Naver ->
            openExternalBrowser(context, "https://www.naver.com")
        SeniorHomeButtonType.Daum ->
            openExternalBrowser(context, "https://www.daum.net")
        SeniorHomeButtonType.Google ->
            openExternalBrowser(context, "https://www.google.com")
        SeniorHomeButtonType.Schedule,
        SeniorHomeButtonType.ChatBuddy,
        SeniorHomeButtonType.Medication,
        SeniorHomeButtonType.Photo,
        SeniorHomeButtonType.Emergency -> Unit
        else -> Unit
    }
}

internal fun openExternalBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    startIntent(context, intent)
}

internal fun openSystemGallery(context: Context) {
    val galleryIntent = Intent(
        Intent.ACTION_VIEW,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    ).apply {
        type = "image/*"
    }

    try {
        context.startActivity(galleryIntent)
    } catch (_: ActivityNotFoundException) {
        val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startIntent(context, fallbackIntent)
    }
}

internal fun openAppOrPlayStore(context: Context, packageName: String) {
    val launchIntent = context.packageManager
        .getLaunchIntentForPackage(packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    if (launchIntent != null && runCatching {
            context.startActivity(launchIntent)
        }.isSuccess
    ) {
        return
    }

    val packageLaunchIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (runCatching { context.startActivity(packageLaunchIntent) }.isSuccess) {
        return
    }

    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName"),
    ).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (runCatching { context.startActivity(marketIntent) }.isSuccess) return

    startIntent(
        context,
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
    )
}

private fun startIntent(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent) }
}
