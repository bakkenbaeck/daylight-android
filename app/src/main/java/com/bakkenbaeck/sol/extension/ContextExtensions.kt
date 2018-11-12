package com.bakkenbaeck.sol.extension

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import java.lang.IllegalStateException

fun Context.getNotificationService(): NotificationManager {
    return getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: throw IllegalStateException("Could not get notification service")
}

fun Context.getAlarmService(): AlarmManager {
    return getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: throw IllegalStateException("Could not get alarm service")
}

fun Context.isLocationPermissionGranted(): Boolean {
    val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    val getPermissionState = ActivityCompat.checkSelfPermission(this, locationPermission)
    return getPermissionState == PackageManager.PERMISSION_GRANTED
}