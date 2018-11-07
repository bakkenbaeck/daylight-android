package com.bakkenbaeck.sol.repository

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.location.Location
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.util.DailyMessage
import com.bakkenbaeck.sol.util.SolPreferences
import com.bakkenbaeck.sol.util.ThreeDayPhases
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationRepository(context: Context) {

    private val prefs: SolPreferences by lazy { SolPreferences(context) }
    private val dailyMessage: DailyMessage by lazy { DailyMessage(context) }
    private val locationClient = createGoogleApiClient(context)

    val notification by lazy { MutableLiveData<String>() }
    val locationIntent by lazy { MutableLiveData<Intent>() }
    val sunrise by lazy { MutableLiveData<Long>() }

    private fun createGoogleApiClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    fun refreshLocation(displayNotification: Boolean) {
        try {
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

        val intentUpdate = Intent().apply {
            action = BaseApplication.ACTION_UPDATE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(BaseApplication.EXTRA_DAILY_MESSAGE, todaysMessage)
            putExtra(BaseApplication.EXTRA_SUNRISE_TIME, threeDayPhases.todaysSunriseAsLong)
            putExtra(BaseApplication.EXTRA_SUNSET_TIME, threeDayPhases.todaysSunsetAsLong)
            putExtra(BaseApplication.EXTRA_LOCATION_MESSAGE, locationMessage)
            putExtra(BaseApplication.EXTRA_CURRENT_PHASE, currentPhaseName)
        }

        locationIntent.postValue(intentUpdate)

        if (displayNotification) {
            notification.postValue(todaysMessage)
        }

        sunrise.postValue(tomorrowsSunrise)
    }

    private fun storeLocation(location: Location?): Location {
        if (location != null) {
            prefs.cacheLocation(location)
            return location
        }

        return prefs.cachedLocation
    }
}