package com.bakkenbaeck.sol.service

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.extension.getAlarmService
import com.bakkenbaeck.sol.extension.isLocationPermissionGranted
import com.bakkenbaeck.sol.util.DailyMessage
import com.bakkenbaeck.sol.util.SolPreferences
import com.bakkenbaeck.sol.util.ThreeDayPhases
import com.bakkenbaeck.sol.view.notification.TodaysMessageNotification

class LocationIntentService : IntentService("LocationIntentService") {

    companion object {
        const val DISPLAY_NOTIFICATION = "display notifications"

        fun start(context: Context, displayNotification: Boolean = false) {
            val intent = Intent(context, LocationIntentService::class.java).apply {
                if (displayNotification) putExtra(DISPLAY_NOTIFICATION, true)
            }
            context.startService(intent)
        }
    }

    private val prefs: SolPreferences by lazy { SolPreferences(applicationContext) }
    private val dailyMessage: DailyMessage by lazy { DailyMessage(applicationContext) }

    override fun onHandleIntent(intent: Intent) {
        val displayNotification = intent.getBooleanExtra(DISPLAY_NOTIFICATION, false)
        refreshLocation(displayNotification)
    }

    fun refreshLocation(displayNotification: Boolean) {
        try {
            val locationClient = BaseApplication.instance.locationClient
            if (!isLocationPermissionGranted()) return
            locationClient.lastLocation.addOnCompleteListener {
                updateLocation(it.result, displayNotification)
            }
        } catch (ex: SecurityException) {
            updateLocation(null, displayNotification)
        }
    }

    private fun updateLocation(location: Location?, displayNotification: Boolean) {
        val safeLocation = storeLocation(location)
//        if (!shouldRefresh()) return
        val threeDayPhases = ThreeDayPhases(safeLocation)
        val tomorrowsSunrise = threeDayPhases.tomorrowsSunrise

        val currentPhaseName = threeDayPhases.currentPhase.name
        val todaysMessage = dailyMessage.generate(threeDayPhases)
        val locationMessage = dailyMessage.getLocation(safeLocation.latitude, safeLocation.longitude)

        broadcastLocationChanged(todaysMessage, threeDayPhases, locationMessage, currentPhaseName)

        if (displayNotification) {
            tryAndShowNotification(todaysMessage)
        }

        tryAndEnableTomorrowsAlarm(tomorrowsSunrise)
    }

    private fun storeLocation(location: Location?): Location {
        if (location != null) {
            prefs.cacheLocation(location)
            return location
        }

        return prefs.cachedLocation
    }

    private fun broadcastLocationChanged(todaysMessage: String, threeDayPhases: ThreeDayPhases, locationMessage: String, currentPhaseName: String) {
        val intent = Intent().apply {
            action = BaseApplication.ACTION_UPDATE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(BaseApplication.EXTRA_DAILY_MESSAGE, todaysMessage)
            putExtra(BaseApplication.EXTRA_SUNRISE_TIME, threeDayPhases.todaysSunriseAsLong)
            putExtra(BaseApplication.EXTRA_SUNSET_TIME, threeDayPhases.todaysSunsetAsLong)
            putExtra(BaseApplication.EXTRA_LOCATION_MESSAGE, locationMessage)
            putExtra(BaseApplication.EXTRA_CURRENT_PHASE, currentPhaseName)
        }

        sendBroadcast(intent)
    }

    private fun tryAndShowNotification(todaysMessage: String) {
        val solPreferences = SolPreferences(this)
        val notificationEnabled = solPreferences.showNotification
        if (!notificationEnabled) {
            return
        }

        TodaysMessageNotification.show(baseContext, todaysMessage)
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
}