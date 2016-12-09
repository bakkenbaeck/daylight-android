package com.bakkenbaeck.sol.util;

import com.bakkenbaeck.sol.R;
import com.florianmski.suncalc.SunCalc;
import com.florianmski.suncalc.models.SunPhase;

import java.util.Calendar;
import java.util.Date;

public class SunPhaseUtil {
    public static int getBackgroundColor(final String theme) {
        int color;

        if (theme.equals("Twilight Astronomical")) {
            color = R.color.twilight_astronomical;
        } else if(theme.equals("Twilight Nautical")){
            color = R.color.twilight_nautical;
        } else if(theme.equals("Twilight Civil")) {
            color = R.color.twilight_civil;
        } else if(theme.equals("Sunrise")) {
            color = R.color.sunrise;
        } else if(theme.equals("Daylight")) {
            color = R.color.daylight;
        } else if(theme.equals("Golden Hour")) {
            color = R.color.golden_hour;
        } else if(theme.equals("Sunset")) {
            color = R.color.sunset;
        } else {
            color = R.color.night;
        }

        return color;
    }

    public static int getPriColor(final String theme) {
        int color;

        if (theme.equals("Twilight Astronomical")) {
            color = R.color.twilight_text2;
        } else if(theme.equals("Twilight Nautical")){
            color = R.color.twilight_text2;
        } else if(theme.equals("Twilight Civil")) {
            color = R.color.twilight_text2;
        } else if(theme.equals("Sunrise")) {
            color = R.color.sunrise_text2;
        } else if(theme.equals("Daylight")) {
            color = R.color.daylight_text2;
        } else if(theme.equals("Golden Hour")) {
            color = R.color.daylight_text2;
        } else if(theme.equals("Sunset")) {
            color = R.color.sunset_text2;
        } else {
            color = R.color.night_text2;
        }

        return color;
    }

    public static int getSecColor(final String theme) {
        int color;

        if (theme.equals("Twilight Astronomical")) {
            color = R.color.twilight_text;
        } else if(theme.equals("Twilight Nautical")){
            color = R.color.twilight_text;
        } else if(theme.equals("Twilight Civil")) {
            color = R.color.twilight_text;
        } else if(theme.equals("Sunrise")) {
            color = R.color.sunrise_text;
        } else if(theme.equals("Daylight")) {
            color = R.color.daylight_text;
        } else if(theme.equals("Golden Hour")) {
            color = R.color.daylight_text;
        } else if(theme.equals("Sunset")) {
            color = R.color.sunset_text;
        } else {
            color = R.color.night_text;
        }

        return color;
    }

    public static SunPhase getSunPhase(final double lat, final double lon) {
        Calendar currentDate = Calendar.getInstance();
        SunPhase phase = SunPhase.get(SunPhase.Name.GOLDEN_HOUR_EVENING);

        for (SunPhase sunPhase : SunCalc.getPhases(currentDate, lat, lon)) {
            Date phaseStartDate = sunPhase.getStartDate().getTime();
            Date phaseEndDate = sunPhase.getEndDate().getTime();

            if (currentDate.getTime().getTime() >= phaseStartDate.getTime()
                    && currentDate.getTime().getTime() <= phaseEndDate.getTime()) {
                phase = sunPhase;
            }
        }

        return phase;
    }
}