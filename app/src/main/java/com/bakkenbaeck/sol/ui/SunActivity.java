package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.text.Spanned;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivitySunBinding;
import com.bakkenbaeck.sol.location.CurrentCity;
import com.bakkenbaeck.sol.util.DailyMessage;
import com.bakkenbaeck.sol.location.SunriseSunset;
import com.bakkenbaeck.sol.location.TimezoneMapper;
import com.bakkenbaeck.sol.util.DayLengthDifference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.Seconds;

public class SunActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private GoogleApiClient googleApiClient;
    private ActivitySunBinding binding;
    private CurrentCity currentCity;
    private DailyMessage dailyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        startGoogleApiClient();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_sun);
        this.currentCity = new CurrentCity(this);
        this.dailyMessage = new DailyMessage(this);
    }

    public void startGoogleApiClient() {
        if (this.googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        this.googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        this.googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(final @Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        getUsersLocation();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUsersLocation();
        } else {
            useDefaultLocation();
        }
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
        final Location defaultLocation = new android.location.Location("");
        defaultLocation.setLatitude(59.9139);
        defaultLocation.setLongitude(10.7522);
        updateLocation(defaultLocation);
    }

    private void updateLocation(final Location location) {
        final String timezone = TimezoneMapper.latLngToTimezoneString(location.getLatitude(), location.getLongitude());
        final DateTimeZone dateTimeZone = DateTimeZone.forID(timezone);

        final DateTime today = DateTime.now(dateTimeZone);
        final DateTime yesterday = today.minusDays(1);

        final Period todayLength = getDayLengthForLocation(location, today, dateTimeZone);
        final Period yesterdayLength = getDayLengthForLocation(location, yesterday, dateTimeZone);
        final Period dayLengthChange = todayLength.minus(yesterdayLength);

        final String nearestCity = this.currentCity.get(location.getLatitude(), location.getLongitude());
        final Spanned todaysMessage = this.dailyMessage.get(nearestCity, dayLengthChange);
        this.binding.todaysMessage.setText(todaysMessage);

        final String todaysDate = DateTime.now(dateTimeZone).toString("dd. MM. YYYY");
        this.binding.todaysDate.setText(todaysDate);
    }

    private Period getDayLengthForLocation(final Location location,
                                           final DateTime day,
                                           final DateTimeZone timezone) {
        final DateTime[] sunriseSunset = SunriseSunset.getSunriseSunsetDateTimes(
                day,
                location.getLatitude(),
                location.getLongitude(),
                timezone);
        final DateTime sunrise = sunriseSunset[0];
        final DateTime sunset = sunriseSunset[1];
        return new Duration(sunrise, sunset).toPeriod();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        useDefaultLocation();
    }
}
