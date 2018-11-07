package com.bakkenbaeck.sol

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.text.Html
import android.util.Log
import com.bakkenbaeck.sol.extension.getAlarmService
import com.bakkenbaeck.sol.extension.getNotificationService
import com.bakkenbaeck.sol.repository.LocationRepository
import com.bakkenbaeck.sol.service.AlarmReceiver
import com.bakkenbaeck.sol.util.SolPreferences
import com.bakkenbaeck.sol.view.SunActivity

class BaseApplication : Application() {

    companion object {
        const val ACTION_UPDATE = "com.bakkenbaeck.sol.UPDATE"
        const val EXTRA_DAILY_MESSAGE = "daily_message"
        const val EXTRA_SUNRISE_TIME = "sunrise_time"
        const val EXTRA_SUNSET_TIME = "sunset_time"
        const val EXTRA_LOCATION_MESSAGE = "location_message"
        const val EXTRA_CURRENT_PHASE = "current_phase"

        lateinit var instance: BaseApplication
    }

    lateinit var locationRepository: LocationRepository

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        initLocationRepository()
        initCalligraphy()
        initObservers()
    }

    private fun initLocationRepository() {
        locationRepository = LocationRepository(applicationContext)
    }

    private fun initCalligraphy() {
        try {
//            CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
//                    .setDefaultFontPath("fonts/gtamericalight.ttf")
//                    .setFontAttrId(R.attr.fontPath)
//                    .build()
//            )
        } catch (e: Exception) {
            Log.d("BaseApplication", "Could not add font")
        }
    }

    private fun initObservers() {
        locationRepository.notification.observeForever {
            if (it != null) tryAndShowNotification(it)
        }
        locationRepository.locationIntent.observeForever {
            if (it != null) sendBroadcast(it)
        }
        locationRepository.sunrise.observeForever {
            if (it != null) tryAndEnableTomorrowsAlarm(it)
        }
    }

    fun refreshLocation(displayNotification: Boolean) {
        locationRepository.refreshLocation(displayNotification)
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

    private fun stripHtml(html: String): String = Html.fromHtml(html).toString()

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
