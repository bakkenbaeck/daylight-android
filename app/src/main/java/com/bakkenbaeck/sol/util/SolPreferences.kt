package com.bakkenbaeck.sol.util


import android.content.Context
import android.content.SharedPreferences
import android.location.Location

class SolPreferences(context: Context) {

    companion object {
        private const val LAT_PREF = "lat_pref"
        private const val LON_PREF = "lon_pref"
        private const val SHOW_NOTIFICATIONS = "show_notifications"

        private const val DEFAULT_LAT = 59.9139
        private const val DEFAULT_LON = 10.7522
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("solPref", Context.MODE_PRIVATE)
    }

    val cachedLocation: Location by lazy { createLocationFromCachedLatLong() }

    private val cachedLatitude: Double by lazy { getDouble(LAT_PREF, DEFAULT_LAT) }
    private val cachedLongitude: Double by lazy { getDouble(LON_PREF, DEFAULT_LON) }

    private fun createLocationFromCachedLatLong(): Location {
        return Location("").apply {
            latitude = cachedLatitude
            longitude = cachedLongitude
        }
    }

    var showNotification: Boolean
        get() = prefs.getBoolean(SHOW_NOTIFICATIONS, true)
        set(showNotification) = prefs.edit().putBoolean(SHOW_NOTIFICATIONS, showNotification).apply()

    fun cacheLocation(location: Location) {
        cacheLatitude(location.latitude)
        cacheLongitude(location.longitude)
    }

    private fun cacheLatitude(latitude: Double) {
        putDouble(LAT_PREF, latitude)
    }

    private fun cacheLongitude(longitude: Double) {
        putDouble(LON_PREF, longitude)
    }

    private fun putDouble(key: String, value: Double) {
        prefs.edit().putLong(key, java.lang.Double.doubleToRawLongBits(value)).apply()
    }

    private fun getDouble(key: String, defaultValue: Double): Double {
        val long = prefs.getLong(key, java.lang.Double.doubleToLongBits(defaultValue))
        return java.lang.Double.longBitsToDouble(long)
    }
}
