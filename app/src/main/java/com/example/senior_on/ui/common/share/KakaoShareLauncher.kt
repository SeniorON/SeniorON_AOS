package com.example.senior_on.ui.common.share

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.example.senior_on.BuildConfig
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate

object KakaoShareLauncher {
    fun launch(
        context: Context,
        content: ShareContent,
        onResult: (ShareLaunchResult) -> Unit,
    ) {
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            onResult(
                ShareLaunchResult.Failure(
                    userMessage = "카카오 네이티브 앱 키가 설정되지 않았어요.",
                ),
            )
            return
        }

        val template = TextTemplate(
            text = content.text,
            link = Link(
                androidExecutionParams = content.androidExecutionParams,
            ),
        )

        runCatching {
            ShareClient.instance.isKakaoTalkSharingAvailable(context)
        }.onFailure {
            onResult(
                ShareLaunchResult.Failure(
                    userMessage = "카카오톡 공유를 시작하지 못했어요.",
                ),
            )
        }.onSuccess { isKakaoTalkAvailable ->
            if (isKakaoTalkAvailable) {
                shareWithKakaoTalk(
                    context = context,
                    template = template,
                    onResult = onResult,
                )
            } else {
                shareWithWeb(
                    context = context,
                    template = template,
                    onResult = onResult,
                )
            }
        }
    }

    private fun shareWithKakaoTalk(
        context: Context,
        template: TextTemplate,
        onResult: (ShareLaunchResult) -> Unit,
    ) {
        ShareClient.instance.shareDefault(context, template) { result, error ->
            if (error != null || result == null) {
                onResult(
                    ShareLaunchResult.Failure(
                        userMessage = "카카오톡 공유를 시작하지 못했어요.",
                    ),
                )
                return@shareDefault
            }

            runCatching {
                val intent = result.intent.apply {
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(intent)
            }.fold(
                onSuccess = { onResult(ShareLaunchResult.Success) },
                onFailure = {
                    onResult(
                        ShareLaunchResult.Failure(
                            userMessage = "카카오톡을 열지 못했어요.",
                        ),
                    )
                },
            )
        }
    }

    private fun shareWithWeb(
        context: Context,
        template: TextTemplate,
        onResult: (ShareLaunchResult) -> Unit,
    ) {
        runCatching {
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(template)
            try {
                KakaoCustomTabsClient.openWithDefault(context, sharerUrl)
            } catch (_: UnsupportedOperationException) {
                KakaoCustomTabsClient.open(context, sharerUrl)
            }
        }.fold(
            onSuccess = { onResult(ShareLaunchResult.Success) },
            onFailure = { throwable ->
                val userMessage = if (throwable is ActivityNotFoundException) {
                    "카카오톡 또는 웹 브라우저를 찾을 수 없어요."
                } else {
                    "카카오톡 공유를 시작하지 못했어요."
                }
                onResult(ShareLaunchResult.Failure(userMessage))
            },
        )
    }
}
