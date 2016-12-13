package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.CurrentCity;

import java.util.Calendar;

public class DailyMessage {


    private final CurrentCity currentCity;
    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
        this.currentCity = new CurrentCity(context);
    }

    public String generate(final ThreeDayPhases threeDayPhases, final Location location) {
        final Calendar dayLengthChange = threeDayPhases.getDayLengthChangeBetweenTodayAndYesterday();
        final String city = getNearestCity(location);
        final String diffText = getDiffText(dayLengthChange);
        final String numMinutesText = getNumberMinutesText(dayLengthChange);

        final CurrentPhase phase = threeDayPhases.getCurrentPhase();
        final int primaryColor = phase.getPrimaryColor();

        return getRawMessage()
                .replace("{color}", String.valueOf(ContextCompat.getColor(context, primaryColor)))
                .replace("{city}", city)
                .replace("{numMinutes}", numMinutesText)
                .replace("{moreOrLess}", diffText);
    }

    private String getNearestCity(final Location location) {
        return this.currentCity.get(location.getLatitude(), location.getLongitude());
    }

    public String getLocation(final double latitude, final double longitude) {
        return currentCity.getCityAndCountry(latitude, longitude);
    }

    @NonNull
    private String getDiffText(final Calendar dayLengthChange) {
        final long dayLengthChangeInSeconds = dayLengthChange.getTimeInMillis() / 1000;

        return dayLengthChangeInSeconds > 0
                ? context.getResources().getString(R.string.more)
                : context.getResources().getString(R.string.less);
    }

    @NonNull
    private String getNumberMinutesText(final Calendar dayLengthChange) {
        final double numMinutes = Math.abs((double)dayLengthChange.getTimeInMillis() / (double)(1000 * 60));
        final int rounedMinutes = (int) Math.round(numMinutes);
        return this.context.getResources().getQuantityString(R.plurals.minutes, rounedMinutes, rounedMinutes);
    }

    private String getRawMessage() {
        final int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        final String[] allMessages = this.context.getResources().getStringArray(R.array.daily_message);
        final int messagePosition = dayOfYear % allMessages.length;
        return allMessages[messagePosition];
    }
}
