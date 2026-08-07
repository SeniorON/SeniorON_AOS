package com.example.senior_on.ui.common.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object MessageShareLauncher {
    fun launch(
        context: Context,
        content: ShareContent,
    ): ShareLaunchResult = runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", content.text)
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }.fold(
        onSuccess = { ShareLaunchResult.Success },
        onFailure = { throwable ->
            when (throwable) {
                is ActivityNotFoundException -> ShareLaunchResult.Failure(
                    userMessage = "문자 앱을 찾을 수 없어요.",
                )
                else -> ShareLaunchResult.Failure(
                    userMessage = "문자 공유를 시작하지 못했어요.",
                )
            }
        },
    )
}
