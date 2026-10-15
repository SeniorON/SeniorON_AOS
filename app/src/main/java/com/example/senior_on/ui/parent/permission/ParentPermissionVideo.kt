package com.example.senior_on.ui.parent.permission

import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

internal fun ParentPermissionStep.videoResource(): Int = when (this) {
    ParentPermissionStep.BatteryOptimization -> R.raw.permission_battery
    ParentPermissionStep.Notification -> R.raw.permission_notification
    ParentPermissionStep.ForegroundLocation -> R.raw.permission_location
    ParentPermissionStep.BackgroundLocation -> R.raw.permission_background_location
    ParentPermissionStep.SleepingApps -> R.raw.permission_sleeping_apps
    ParentPermissionStep.DefaultHome -> R.raw.permission_default_home
}

@Composable
internal fun ParentPermissionVideo(step: ParentPermissionStep, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var failed by remember(step) { mutableStateOf(false) }
    val video = remember(context, step) { VideoView(context) }
    DisposableEffect(video, lifecycle) {
        video.setOnPreparedListener { player ->
            player.isLooping = true
            player.setVolume(0f, 0f)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) video.start()
            else video.seekTo(1)
        }
        video.setOnErrorListener { _, _, _ -> failed = true; true }
        video.setVideoURI(Uri.parse("android.resource://${context.packageName}/${step.videoResource()}"))
        onDispose {
            video.setOnPreparedListener(null)
            video.setOnErrorListener(null)
            video.stopPlayback()
        }
    }
    DisposableEffect(video, lifecycle) {
        fun synchronizePlayback() {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) video.start()
            else video.pause()
        }
        val observer = LifecycleEventObserver { _, _ -> synchronizePlayback() }
        lifecycle.addObserver(observer)
        synchronizePlayback()
        onDispose { lifecycle.removeObserver(observer); video.pause() }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        if (failed) Text("안내 영상을 재생하지 못했어요.\n아래 설명을 따라 설정해 주세요.",
            style = SeniorOnTextStyles.BodySRegular, color = SeniorOnColors.Gray500)
        else AndroidView(factory = {
            FrameLayout(it).apply {
                addView(video, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER))
            }
        }, modifier = Modifier.fillMaxSize())
    }
}
