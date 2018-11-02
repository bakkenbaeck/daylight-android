package com.bakkenbaeck.sol.service


import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.text.Html
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.getAlarmService
import com.bakkenbaeck.sol.extension.getNotificationService
import com.bakkenbaeck.sol.view.SunActivity
import com.bakkenbaeck.sol.util.DailyMessage
import com.bakkenbaeck.sol.util.SolPreferences
import com.bakkenbaeck.sol.util.ThreeDayPhases
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient


class SunsetService : Service() {

    companion object {
        const val ACTION_UPDATE = "com.example.androidintentservice.UPDATE"
        const val EXTRA_DAILY_MESSAGE = "daily_message"
        const val EXTRA_SUNRISE_TIME = "sunrise_time"
        const val EXTRA_SUNSET_TIME = "sunset_time"
        const val EXTRA_LOCATION_MESSAGE = "location_message"
        const val EXTRA_CURRENT_PHASE = "current_phase"
        const val EXTRA_SHOW_NOTIFICATION = "show_notification"
    }

    private val locationClient by lazy { createGoogleApiClient() }
    private val dailyMessage: DailyMessage by lazy { DailyMessage(this) }
    private val prefs: SolPreferences by lazy { SolPreferences(this) }

    private var shouldTryAndShowNotification: Boolean = false
    private var lastRefreshTime: Long = 0

    private fun createGoogleApiClient(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        shouldTryAndShowNotification = intent.getBooleanExtra(EXTRA_SHOW_NOTIFICATION, false)

        try {
            locationClient.lastLocation.addOnCompleteListener {
                updateLocation(it.result)
            }
        } catch (ex: SecurityException) {
            updateLocation(null)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun updateLocation(location: Location?) {
        val safeLocation = storeLocation(location)
        if (!shouldRefresh()) return
        val threeDayPhases = ThreeDayPhases(safeLocation)
        val tomorrowsSunrise = threeDayPhases.tomorrowsSunrise

        val currentPhaseName = threeDayPhases.currentPhase.name
        val todaysMessage = dailyMessage.generate(threeDayPhases)
        val locationMessage = dailyMessage.getLocation(safeLocation.latitude, safeLocation.longitude)

        val intentUpdate = Intent().apply {
            action = ACTION_UPDATE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(EXTRA_DAILY_MESSAGE, todaysMessage)
            putExtra(EXTRA_SUNRISE_TIME, threeDayPhases.todaysSunriseAsLong)
            putExtra(EXTRA_SUNSET_TIME, threeDayPhases.todaysSunsetAsLong)
            putExtra(EXTRA_LOCATION_MESSAGE, locationMessage)
            putExtra(EXTRA_CURRENT_PHASE, currentPhaseName)
        }

        sendBroadcast(intentUpdate)

        if (this.shouldTryAndShowNotification) {
            tryAndShowNotification(todaysMessage)
        }

        tryAndEnableTomorrowsAlarm(tomorrowsSunrise)

        stopSelf()
    }

    private fun storeLocation(location: Location?): Location {
        if (location != null) {
            this.prefs.cacheLocation(location)
            return location
        }

        return this.prefs.cachedLocation
    }

    private fun shouldRefresh(): Boolean {
        val refreshRate: Long = 5000
        val nextRefreshThreshold = lastRefreshTime + refreshRate
        val shouldRefresh = System.currentTimeMillis() > nextRefreshThreshold
        if (shouldRefresh) {
            lastRefreshTime = System.currentTimeMillis()
        }
        return shouldRefresh
    }

    private fun tryAndShowNotification(todaysMessage: String) {
        val solPreferences = SolPreferences(this)
        val notificationEnabled = solPreferences.showNotification
        if (!notificationEnabled) {
            return
        }

        showNotification(todaysMessage)
    }

    private fun showNotification(todaysMessage: String) {
        val resultIntent = Intent(this, SunActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.daylight_icon)
        val contentText = stripHtml(todaysMessage)
        val bigTextStyle = NotificationCompat.BigTextStyle()
                .setBigContentTitle(resources.getString(R.string.app_name))
                .bigText(contentText)

        val mBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.daylight_notification_icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(contentText)
                .setAutoCancel(true)
                .setStyle(bigTextStyle)
                .setContentIntent(resultPendingIntent)

        val notificationManager = getNotificationService()
        notificationManager.notify(1, mBuilder.build())
    }

    private fun tryAndEnableTomorrowsAlarm(alarmTime: Long) {
        val solPreferences = SolPreferences(this)
        val notificationEnabled = solPreferences.showNotification
        if (!notificationEnabled) return

        enableTomorrowsAlarm(alarmTime)
    }

    private fun enableTomorrowsAlarm(alarmTime: Long) {
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0)
        val manager = getAlarmService()
        manager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }

    private fun stripHtml(html: String): String = Html.fromHtml(html).toString()
}
