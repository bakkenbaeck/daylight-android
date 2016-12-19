package com.bakkenbaeck.sol.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

public class SolPreferences {

    private static final String LAT_PREF = "lat_pref";
    private static final String LON_PREF = "lon_pref";
    private static final String SHOW_NOTIFICATIONS = "show_notifications";

    private static final double DEFAULT_LAT = 59.9139;
    private static final double DEFAULT_LON = 10.7522;

    private final SharedPreferences prefs;

    public SolPreferences(final Context context) {
        this.prefs = context.getSharedPreferences("solPref", Context.MODE_PRIVATE);
    }

    public void cacheLocation(final Location location) {
        cacheLatitude(location.getLatitude());
        cacheLongitude(location.getLongitude());
    }

    private void cacheLatitude(final double latitude) {
        putDouble(LAT_PREF, latitude);
    }

    private void cacheLongitude(final double longitude) {
        putDouble(LON_PREF, longitude);
    }

    public Location getCachedLocation() {
        final Location location = new Location("");
        location.setLatitude(getCachedLatitude());
        location.setLongitude(getCachedLongitude());
        return location;
    }

    private double getCachedLatitude() {
        return getDouble(LAT_PREF, DEFAULT_LAT);
    }

    private double getCachedLongitude() {
        return getDouble(LON_PREF, DEFAULT_LON);
    }

    private void putDouble(final String key, final double value) {
        this.prefs.edit().putLong(key, Double.doubleToRawLongBits(value)).apply();
    }

    private double getDouble(final String key, final double defaultValue) {
        return Double.longBitsToDouble(this.prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public void setShowNotification(final boolean showNotification) {
        prefs.edit().putBoolean(SHOW_NOTIFICATIONS, showNotification).apply();
    }

    public boolean getShowNotification() {
        return prefs.getBoolean(SHOW_NOTIFICATIONS, false);
    }
}
