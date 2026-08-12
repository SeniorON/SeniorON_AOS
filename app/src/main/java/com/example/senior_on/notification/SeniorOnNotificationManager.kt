package com.example.senior_on.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.senior_on.MainActivity
import com.example.senior_on.R
import com.google.firebase.messaging.RemoteMessage

object SeniorOnNotificationManager {
    private const val AlertChannelId = "senior_on_alerts"
    private const val DefaultTitle = "SeniorON 알림"
    private const val DefaultBody = "새로운 알림이 도착했습니다."
    private val SingleVibrationPattern = longArrayOf(0L, 500L)

    fun createAlertChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            AlertChannelId,
            context.getString(R.string.senior_on_alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(
                R.string.senior_on_alert_channel_description
            )
            enableVibration(true)
            vibrationPattern = SingleVibrationPattern
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null,
            )
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showRemoteMessage(
        context: Context,
        message: RemoteMessage,
    ) {
        if (!canPostNotifications(context)) return

        createAlertChannel(context)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: DefaultTitle
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: DefaultBody
        val notificationId = message.data["notificationId"]
            ?.toIntOrNull()
            ?: message.data["eventId"]?.toIntOrNull()
            ?: message.messageId?.hashCode()
            ?: System.currentTimeMillis().hashCode()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            action = NotificationNavigationEventStore.OpenNotificationAction
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            message.data.forEach { (key, value) -> putExtra(key, value) }
            putExtra(NotificationNavigationEventStore.TitleKey, title)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, AlertChannelId)
            .setSmallIcon(R.drawable.ic_system_notification)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_notification_large,
                )
            )
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(SingleVibrationPattern)
            .setSound(
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                )
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            notificationId,
            notification,
        )
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
}
