package com.bakkenbaeck.sol

import android.app.Application
import android.util.Log
import com.bakkenbaeck.sol.service.LocationIntentService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

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

    val locationClient by lazy { createGoogleApiClient() }

    init {
        instance = this
    }

    private fun createGoogleApiClient(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        initCalligraphy()
    }

    private fun initCalligraphy() {
        try {
            CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                    .setDefaultFontPath("fonts/gtamericalight.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            )
        } catch (e: Exception) {
            Log.d("BaseApplication", "Could not add font")
        }
    }

    fun refreshLocation(displayNotification: Boolean) {
        LocationIntentService.start(applicationContext, displayNotification)
    }
}
