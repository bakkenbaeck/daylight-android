package com.bakkenbaeck.sol.util;

import com.florianmski.suncalc.SunCalc;
import com.florianmski.suncalc.models.SunPhase;

import java.util.Calendar;
import java.util.Date;

public class SunPhaseUtil {

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

    public static SunPhase getSunPhase(final double lat, final double lon, final String phaseName) {
        Calendar currentDate = Calendar.getInstance();
        SunPhase phase = SunPhase.get(SunPhase.Name.GOLDEN_HOUR_EVENING);

        for (SunPhase sunPhase : SunCalc.getPhases(currentDate, lat, lon)) {
            if (phaseName.equals(sunPhase.getName().toString())) {
                return sunPhase;
            }
        }

        return phase;
    }
}