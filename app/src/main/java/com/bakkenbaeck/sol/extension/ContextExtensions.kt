package com.bakkenbaeck.sol.extension

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import java.lang.IllegalStateException

fun Context.getNotificationService(): NotificationManager {
    return getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw IllegalStateException("Could not get notification service")
}

fun Context.getAlarmService(): AlarmManager {
    return getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: throw IllegalStateException("Could not get alarm service")
}