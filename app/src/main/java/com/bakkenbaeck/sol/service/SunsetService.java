package com.bakkenbaeck.sol.service;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.TimezoneMapper;
import com.bakkenbaeck.sol.util.DailyMessage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class SunsetService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_UPDATE = "com.example.androidintentservice.UPDATE";
    public static final String EXTRA_DAILY_MESSAGE = "daily_message";
    public static final String EXTRA_TODAYS_DATE = "todays_date";
    public static final String EXTRA_TOMORROWS_SUNRISE = "tomorrows_sunrise";
    public static final String EXTRA_SHOW_NOTIFICATION = "show_notification";

    private GoogleApiClient googleApiClient;
    private DailyMessage dailyMessage;
    private boolean showNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.dailyMessage = new DailyMessage(this);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        this.showNotification = intent.getBooleanExtra(EXTRA_SHOW_NOTIFICATION, false);
        this.googleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.googleApiClient.disconnect();
    }

    @Override
    public void onConnected(final @Nullable Bundle bundle) {
        getUsersLocation();
    }

    private void getUsersLocation() {
        try {
            final Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(this.googleApiClient);
            updateLocation(currentLocation);
        } catch (final SecurityException ex) {
            useDefaultLocation();
        }
    }

    private void useDefaultLocation() {
        final Location defaultLocation = new  android.location.Location("");
        defaultLocation.setLatitude(59.9139);
        defaultLocation.setLongitude(10.7522);
        updateLocation(defaultLocation);
    }

    private void updateLocation(final Location location) {
        final String timezone = TimezoneMapper.latLngToTimezoneString(location.getLatitude(), location.getLongitude());
        final DateTimeZone dateTimeZone = DateTimeZone.forID(timezone);

        final String todaysMessage = this.dailyMessage.generate(location);
        final String todaysDate = DateTime.now(dateTimeZone).toString("dd. MM. YYYY");
        final long tomorrowsSunrise = this.dailyMessage.getTomorrowsSunrise(location);

        final Intent intentUpdate = new Intent();
        intentUpdate.setAction(ACTION_UPDATE);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(EXTRA_DAILY_MESSAGE, todaysMessage);
        intentUpdate.putExtra(EXTRA_TODAYS_DATE, todaysDate);
        intentUpdate.putExtra(EXTRA_TOMORROWS_SUNRISE, tomorrowsSunrise);
        sendBroadcast(intentUpdate);

        if (this.showNotification) {
            showNotification(todaysMessage);
        }

        stopSelf();
    }

    private void showNotification(final String todaysMessage) {
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(stripHtml(todaysMessage));
        final NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, mBuilder.build());
    }

    public String stripHtml(final String html) {
        return Html.fromHtml(html).toString();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        useDefaultLocation();
    }
}
