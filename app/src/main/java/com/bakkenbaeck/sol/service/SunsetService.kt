package com.bakkenbaeck.sol.service


import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.text.Html

import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.getAlarmService
import com.bakkenbaeck.sol.extension.getNotificationService
import com.bakkenbaeck.sol.ui.SunActivity
import com.bakkenbaeck.sol.util.DailyMessage
import com.bakkenbaeck.sol.util.SolPreferences
import com.bakkenbaeck.sol.util.ThreeDayPhases
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

class SunsetService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        const val ACTION_UPDATE = "com.example.androidintentservice.UPDATE"
        const val EXTRA_DAILY_MESSAGE = "daily_message"
        const val EXTRA_SUNRISE_TIME = "sunrise_time"
        const val EXTRA_SUNSET_TIME = "sunset_time"
        const val EXTRA_LOCATION_MESSAGE = "location_message"
        const val EXTRA_CURRENT_PHASE = "current_phase"
        const val EXTRA_SHOW_NOTIFICATION = "show_notification"
    }

    private val googleApiClient: GoogleApiClient by lazy { createGoogleApiClient() }
    private val dailyMessage: DailyMessage by lazy { DailyMessage(this) }
    private val prefs: SolPreferences by lazy { SolPreferences(this) }

    private var shouldTryAndShowNotification: Boolean = false

    private fun createGoogleApiClient(): GoogleApiClient {
        return GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        shouldTryAndShowNotification = intent.getBooleanExtra(EXTRA_SHOW_NOTIFICATION, false)
        googleApiClient.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        this.googleApiClient.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        getUsersLocation()
    }

    private fun getUsersLocation() {
        try {
            val currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            updateLocation(currentLocation)
        } catch (ex: SecurityException) {
            updateLocation(null)
        }

    }

    private fun updateLocation(location: Location?) {
        val safeLocation = storeLocation(location)
        val threeDayPhases = ThreeDayPhases().init(safeLocation)
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

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        updateLocation(null)
    }
}
