package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.CurrentCity;
import com.bakkenbaeck.sol.location.SunriseSunset;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.Seconds;

public class DailyMessage {

    private final CurrentCity currentCity;
    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
        this.currentCity = new CurrentCity(context);
    }

    public long getTomorrowsSunrise(final Location location, final DateTimeZone dateTimeZone) {
        final DateTime tomorrow = DateTime.now(dateTimeZone).plusDays(1);
        final DateTime[] sunriseSunset = SunriseSunset.getSunriseSunsetDateTimes(
                tomorrow,
                location.getLatitude(),
                location.getLongitude(),
                dateTimeZone);

        return sunriseSunset[1].getMillis();
    }

    public String generate(final Location location, final DateTimeZone dateTimeZone) {
        final Period dayLengthChange = getDayLengthChangeBetweenTodayAndYesterday(location, dateTimeZone);
        final String city = getNearestCity(location);
        final String diffText = getDiffText(dayLengthChange);
        final String numMinutesText = getNumberMinutesText(dayLengthChange);
        final String tense = getCorrectTense(location, dateTimeZone);

        return getRawMessage()
                .replace("{city}", city)
                .replace("{numMinutes}", numMinutesText)
                .replace("{moreOrLess}", diffText)
                .replace("{tense}", tense);
    }

    private Period getDayLengthChangeBetweenTodayAndYesterday(final Location location, final DateTimeZone dateTimeZone) {
        final DateTime today = DateTime.now(dateTimeZone);
        final DateTime yesterday = today.minusDays(1);

        final Period todayLength = getDayLengthForLocation(location, today, dateTimeZone);
        final Period yesterdayLength = getDayLengthForLocation(location, yesterday, dateTimeZone);
        return todayLength.minus(yesterdayLength);
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

    private String getNearestCity(final Location location) {
        return this.currentCity.get(location.getLatitude(), location.getLongitude());
    }

    @NonNull
    private String getDiffText(final Period dayLengthChange) {
        final Seconds dayLengthChangeInSeconds = dayLengthChange.toStandardSeconds();

        return
                dayLengthChangeInSeconds.isGreaterThan(Seconds.ZERO)
                        ? context.getResources().getString(R.string.more)
                        : context.getResources().getString(R.string.less);
    }

    @NonNull
    private String getNumberMinutesText(final Period dayLengthChange) {
        final int numMinutes =
                Math.abs(dayLengthChange.getMinutes())
                        + Math.abs(dayLengthChange.getSeconds()) / 30;
        return this.context.getResources().getQuantityString(R.plurals.minutes, numMinutes, numMinutes);
    }

    private String getCorrectTense(final Location location, final DateTimeZone dateTimeZone) {
        final DateTime[] sunriseSunset = SunriseSunset.getSunriseSunsetDateTimes(
                DateTime.now(dateTimeZone),
                location.getLatitude(),
                location.getLongitude(),
                dateTimeZone);
        final DateTime sunset = sunriseSunset[1];

        return DateTime.now(dateTimeZone).isAfter(sunset)
                ? this.context.getResources().getString(R.string.pastTense)
                : this.context.getResources().getString(R.string.presentTense);
    }

    private String getRawMessage() {
        final int dayOfYear = DateTime.now().dayOfYear().get();
        final String[] allMessages = this.context.getResources().getStringArray(R.array.daily_message);
        final int messagePosition = dayOfYear % allMessages.length;
        return allMessages[messagePosition];
    }
}
