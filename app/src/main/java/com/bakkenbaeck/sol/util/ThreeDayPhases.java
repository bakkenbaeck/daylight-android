package com.bakkenbaeck.sol.util;


import android.location.Location;

import com.florianmski.suncalc.SunCalc;
import com.florianmski.suncalc.models.SunPhase;

import org.joda.time.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ThreeDayPhases {

    private final static int SUNRISE = 4;
    private final static int SUNSET = 8;

    private final Calendar today;
    private final Calendar tomorrow;
    private final Calendar yesterday;

    private List<SunPhase> yesterdaysSunPhases;
    private List<SunPhase> todaysSunPhases;
    private List<SunPhase> tomorrowsSunPhases;
    private long lastRefreshTime = 0;

    public ThreeDayPhases() {
        this.today = Calendar.getInstance();
        this.yesterday = Calendar.getInstance();
        this.tomorrow = Calendar.getInstance();

        this.yesterday.add(Calendar.DATE, -1);
        this.tomorrow.add(Calendar.DATE, 1);
    }

    public ThreeDayPhases init(final Location location) {
        if (!shouldRefresh()) {
            return this;
        }

        final double lat = location.getLatitude();
        final double lon = location.getLongitude();

        this.yesterdaysSunPhases = SunCalc.getPhases(this.yesterday, lat, lon);
        this.todaysSunPhases = SunCalc.getPhases(this.today, lat, lon);
        this.tomorrowsSunPhases = SunCalc.getPhases(this.tomorrow, lat, lon);
        return this;
    }

    public Period getDayLengthChangeBetweenTodayAndYesterday() {
        final Period todayLength = getDayLengthForPhases(this.todaysSunPhases);
        final Period yesterdayLength = getDayLengthForPhases(this.yesterdaysSunPhases);
        return todayLength.minus(yesterdayLength);
    }

    private Period getDayLengthForPhases(final List<SunPhase> sunPhases) {
        final SunPhase sunrise = sunPhases.get(SUNRISE);
        final SunPhase sunset = sunPhases.get(SUNSET);

        final Date sunriseTime = sunrise.getStartDate().getTime();
        final Date sunsetTime = sunset.getEndDate().getTime();

        final long dayLengthInMillis = sunsetTime.getTime() - sunriseTime.getTime();
        return new Period(dayLengthInMillis);
    }

    private boolean shouldRefresh() {
        final long refreshRate = 5000;
        final long nextRefreshThreshold = lastRefreshTime + refreshRate;
        return System.currentTimeMillis() > nextRefreshThreshold;
    }

    public long getTomorrowsSunrise() {
        final SunPhase sunrise = this.tomorrowsSunPhases.get(SUNRISE);
        return sunrise.getStartDate().getTime().getTime();
    }
}
