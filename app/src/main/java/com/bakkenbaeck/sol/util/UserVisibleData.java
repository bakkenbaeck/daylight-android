package com.bakkenbaeck.sol.util;


import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.service.SunsetService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserVisibleData {

    private final Spanned todaysMessage;
    private final String locationMessage;
    private final CurrentPhase currentPhase;
    private final String sunriseText;
    private final String sunsetText;
    private final double progress;

    public UserVisibleData(final Context context, final Intent intent) {
        final String dailyMessage = intent.getStringExtra(SunsetService.EXTRA_DAILY_MESSAGE);
        this.todaysMessage = convertToHtml(dailyMessage);

        final long sunriseTime = intent.getLongExtra(SunsetService.EXTRA_SUNRISE_TIME, 0);
        final long sunsetTime = intent.getLongExtra(SunsetService.EXTRA_SUNSET_TIME, 0);
        this.locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE);
        this.currentPhase = new CurrentPhase(intent.getStringExtra(SunsetService.EXTRA_CURRENT_PHASE));

        final SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.hh_mm), Locale.getDefault());
        this.sunriseText = sdf.format(sunriseTime);
        this.sunsetText = sdf.format(sunsetTime);

        this.progress = calculateProgress(sunriseTime, sunsetTime);
    }

    private double calculateProgress(final long sunrise, final long sunset) {
        final long span = sunset - sunrise;
        final long current = Calendar.getInstance().getTimeInMillis() - sunrise;
        return (double) current / (double) span;
    }

    private Spanned convertToHtml(final String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    public Spanned getTodaysMessage() {
        return todaysMessage;
    }

    public String getLocationMessage() {
        return locationMessage;
    }

    public String getSunriseText() {
        return sunriseText;
    }

    public String getSunsetText() {
        return sunsetText;
    }

    public CurrentPhase getCurrentPhase() {
        return currentPhase;
    }

    public double getProgress() {
        return progress;
    }
}
