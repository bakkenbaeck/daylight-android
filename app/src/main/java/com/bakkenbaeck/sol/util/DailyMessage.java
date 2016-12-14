package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.location.CurrentCity;
import com.florianmski.suncalc.models.SunPhase;

import java.util.Calendar;
import java.util.Random;

public class DailyMessage {

    private final CurrentCity currentCity;
    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
        this.currentCity = new CurrentCity(context);
    }

    public String generate(final ThreeDayPhases threeDayPhases) {
        final Calendar dayLengthChange = threeDayPhases.getDayLengthChangeBetweenTodayAndYesterday();
        final CurrentPhase phase = threeDayPhases.getCurrentPhase();

        final boolean moreOrLess = getDiff(dayLengthChange);
        final int primaryColor = phase.getPrimaryColor();

        final int minutes = getNumberMinutesText(dayLengthChange);
        final String phaseName = phase.getName();
        final String nightPhase = SunPhase.all().get(ThreeDayPhases.NIGHT).getName().toString();

        String[] messageArray;

        if (phaseName.equals(nightPhase) && moreOrLess) {
            messageArray = MessageUtil.getPositiveNightMessages(context);
        } else if(phaseName.equals(nightPhase) && !moreOrLess) {
            messageArray = MessageUtil.getNegativeNightMessages(context);
        } else if(!(phaseName.equals(nightPhase)) && !moreOrLess) {
            messageArray = MessageUtil.getNegativeDayMessages(context);
        } else {
            messageArray = MessageUtil.getPositiveDayMessages(context);
        }

        final Random ran = new Random();
        final int ranInt = ran.nextInt(messageArray.length);

        String plural = minutes > 1
                ? context.getResources().getString(R.string.minutes)
                : context.getResources().getString(R.string.minute);

        return messageArray[ranInt]
                .replace("{color}", String.valueOf(ContextCompat.getColor(context, primaryColor)))
                .replace("{numMinutes}", String.valueOf(minutes))
                .replace("{minutes}", plural);
    }

    public String getLocation(final double latitude, final double longitude) {
        return currentCity.getCityAndCountry(latitude, longitude);
    }

    @NonNull
    private boolean getDiff(final Calendar dayLengthChange) {
        final long dayLengthChangeInSeconds = dayLengthChange.getTimeInMillis() / 1000;
        return dayLengthChangeInSeconds > 0;
    }

    @NonNull
    private int getNumberMinutesText(final Calendar dayLengthChange) {
        final double numMinutes = Math.abs((double)dayLengthChange.getTimeInMillis() / (double)(1000 * 60));
        final int rounedMinutes = (int) Math.round(numMinutes);
        return rounedMinutes;
    }
}
