package com.bakkenbaeck.sol.view.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.text.Html
import androidx.core.app.NotificationCompat
import com.bakkenbaeck.sol.extension.getNotificationService
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.view.MainActivity

class TodaysMessageNotification {

    companion object {
        private const val CHANNEL_ID = "Today's Message"
        private const val LARGE_ICON = R.mipmap.daylight_icon
        private const val NOTIFICATION_ICON = R.mipmap.daylight_notification_icon
        private const val APP_NAME = R.string.app_name

        fun show(context: Context, message: String) {

            val resultIntent = Intent(context, MainActivity::class.java)
            val resultPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val resources = context.resources
            val largeIcon = BitmapFactory.decodeResource(resources, LARGE_ICON)
            val contentText = stripHtml(message)
            val appName: String = resources.getString(APP_NAME)

            val bigTextStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(appName)
                    .bigText(contentText)

            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(NOTIFICATION_ICON)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(appName)
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setStyle(bigTextStyle)
                    .setContentIntent(resultPendingIntent)

            val notificationManager = context.getNotificationService()
            notificationManager.notify(1, mBuilder.build())
        }

        private fun stripHtml(html: String): String = Html.fromHtml(html).toString()
    }
}