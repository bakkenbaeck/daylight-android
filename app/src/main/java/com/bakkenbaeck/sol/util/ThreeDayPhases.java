package com.bakkenbaeck.sol.util;


import android.location.Location;

import com.florianmski.suncalc.SunCalc;
import com.florianmski.suncalc.models.SunPhase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ThreeDayPhases {

    public final static int NIGHT = 12;
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

    public Calendar getDayLengthChangeBetweenTodayAndYesterday() {
        final Calendar todayLength = getDayLengthForPhases(this.todaysSunPhases);
        final Calendar yesterdayLength = getDayLengthForPhases(this.yesterdaysSunPhases);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(todayLength.getTimeInMillis() - yesterdayLength.getTimeInMillis());

        return c;
    }

    public Calendar getDayLengthChangeBetweenTodayAndTomorrow() {
        final Calendar todayLength = getDayLengthForPhases(this.todaysSunPhases);
        final Calendar tomorrowLength = getDayLengthForPhases(this.tomorrowsSunPhases);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(tomorrowLength.getTimeInMillis() - todayLength.getTimeInMillis());

        return c;
    }

    private Calendar getDayLengthForPhases(final List<SunPhase> sunPhases) {
        final SunPhase sunrise = sunPhases.get(SUNRISE);
        final SunPhase sunset = sunPhases.get(SUNSET);

        final Date sunriseTime = sunrise.getStartDate().getTime();
        final Date sunsetTime = sunset.getEndDate().getTime();
        final long dayLengthInMillis = sunsetTime.getTime() - sunriseTime.getTime();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayLengthInMillis);

        return c;
    }

    public long getTomorrowsSunrise() {
        final SunPhase sunrise = this.tomorrowsSunPhases.get(SUNRISE);
        return sunrise.getStartDate().getTime().getTime();
    }

    public String getTodaysSunriseAsString() {
        final SunPhase sunrise = this.todaysSunPhases.get(SUNRISE);
        return DateUtil.dateFormat("HH:mm", sunrise.getStartDate().getTime());
    }

    public String getTodaysSunsetAsString() {
        final SunPhase sunset = this.todaysSunPhases.get(SUNSET);
        return DateUtil.dateFormat("HH:mm", sunset.getEndDate().getTime());
    }

    private boolean shouldRefresh() {
        final long refreshRate = 5000;
        final long nextRefreshThreshold = lastRefreshTime + refreshRate;
        final boolean shouldRefresh = System.currentTimeMillis() > nextRefreshThreshold;
        if (shouldRefresh) {
            lastRefreshTime = System.currentTimeMillis();
        }
        return shouldRefresh;
    }

    public CurrentPhase getCurrentPhase() {
        final Calendar now = Calendar.getInstance();
        for (final SunPhase phase : this.todaysSunPhases) {
            if (phase.getEndDate().after(now)) {
                return new CurrentPhase(phase);
            }
        }
        return new CurrentPhase(this.todaysSunPhases.get(0));
    }
}
