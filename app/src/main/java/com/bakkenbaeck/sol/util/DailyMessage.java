package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.CurrentCity;
import com.florianmski.suncalc.models.SunPhase;

import java.util.Calendar;

public class DailyMessage {

    private final CurrentCity currentCity;
    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
        this.currentCity = new CurrentCity(context);
    }

    public String generate(final ThreeDayPhases threeDayPhases) {
        final CurrentPhase phase = threeDayPhases.getCurrentPhase();
        final String phaseName = phase.getName();

        final String nightPhaseName = SunPhase.all().get(ThreeDayPhases.NIGHT).getName().toString();
        final Calendar dayLengthChange = phaseName.equals(nightPhaseName)
                ? threeDayPhases.getDayLengthChangeBetweenTodayAndTomorrow()
                : threeDayPhases.getDayLengthChangeBetweenTodayAndYesterday();

        final boolean isDayGettingLonger = isDayGettingLonger(dayLengthChange);
        final int primaryColor = phase.getPrimaryColor();
        final int minutes = getRoundedMinutesForCalendar(dayLengthChange);

        return generateMessage(phaseName, minutes, isDayGettingLonger, primaryColor);
    }

    private String generateMessage(final String phaseName,
                                   final int minutes,
                                   final boolean isDayGettingLonger,
                                   final int primaryColor) {

        String[] messageArray = getMessageArray(minutes, isDayGettingLonger, phaseName);

        final int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        final int messagePosition = dayOfYear % messageArray.length;

        return messageArray[messagePosition]
                .replace("{color}", String.valueOf(ContextCompat.getColor(context, primaryColor)))
                .replace("{numMinutes}", String.valueOf(minutes))
                .replace("{minutes}", plural(minutes));
    }

    private String[] getMessageArray(final int minutes, final boolean isDayGettingLonger, final String phaseName) {
        final String nightPhaseName = SunPhase.all().get(ThreeDayPhases.NIGHT).getName().toString();
        final boolean isLessThanAMinute = minutes == 0;
        final boolean isNight = phaseName.equals(nightPhaseName);

        int messageArrayRef;

        if (isLessThanAMinute) {
            if (isNight && isDayGettingLonger) {
                messageArrayRef = R.array.night_messages_positive_less_than_one_minute;
            } else if (isNight && !isDayGettingLonger) {
                messageArrayRef = R.array.night_messages_negative_less_than_one_minute;
            } else if (!isNight && isDayGettingLonger) {
                messageArrayRef = R.array.daily_messages_positive_less_than_one_minute;
            } else {
                messageArrayRef = R.array.daily_messages_negative_less_than_one_minute;
            }
        } else {
            if (isNight && isDayGettingLonger) {
                messageArrayRef = R.array.night_messages_positive;
            } else if (isNight && !isDayGettingLonger) {
                messageArrayRef = R.array.night_messages_negative;
            } else if (!isNight && isDayGettingLonger) {
                messageArrayRef = R.array.daily_messages_positive;
            } else {
                messageArrayRef = R.array.daily_messages_negative;
            }
        }

        return context.getResources().getStringArray(messageArrayRef);
    }

    private String plural(final int minutes) {
        return minutes > 1
                ? context.getResources().getString(R.string.minutes)
                : context.getResources().getString(R.string.minute);
    }

    public String getLocation(final double latitude, final double longitude) {
        return currentCity.getCityAndCountry(latitude, longitude);
    }

    private boolean isDayGettingLonger(final Calendar dayLengthChange) {
        final long dayLengthChangeInSeconds = dayLengthChange.getTimeInMillis() / 1000;
        return dayLengthChangeInSeconds > 0;
    }

    private int getRoundedMinutesForCalendar(final Calendar dayLengthChange) {
        final double numMinutes = Math.abs((double)dayLengthChange.getTimeInMillis() / (double)(1000 * 60));
        return (int) Math.round(numMinutes);
    }
}
