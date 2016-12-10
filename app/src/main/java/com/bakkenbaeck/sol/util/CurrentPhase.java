package com.bakkenbaeck.sol.util;

import com.bakkenbaeck.sol.R;
import com.florianmski.suncalc.models.SunPhase;

public final class CurrentPhase {

    private final String phaseName;

    public CurrentPhase(final SunPhase phase) {
        this(phase.getName().toString());
    }

    public CurrentPhase(final String phaseName) {
        this.phaseName = phaseName;
    }

    public final String getName() {
        return phaseName;
    }

    public final int getBackgroundColor() {
        switch (phaseName) {
            case "Twilight Astronomical":
                return R.color.twilight_astronomical;
            case "Twilight Nautical":
                return R.color.twilight_nautical;
            case "Twilight Civil":
                return R.color.twilight_civil;
            case "Sunrise":
                return R.color.sunrise;
            case "Daylight":
                return R.color.daylight;
            case "Golden Hour":
                return R.color.golden_hour;
            case "Sunset":
                return R.color.sunset;
            default:
                return R.color.night;
        }
    }

    public final int getPrimaryColor() {
        switch (phaseName) {
            case "Twilight Astronomical":
                return R.color.twilight_text2;
            case "Twilight Nautical":
                return R.color.twilight_text2;
            case "Twilight Civil":
                return R.color.twilight_text2;
            case "Sunrise":
                return R.color.sunrise_text2;
            case "Daylight":
                return R.color.daylight_text2;
            case "Golden Hour":
                return R.color.daylight_text2;
            case "Sunset":
                return R.color.sunset_text2;
            default:
                return R.color.night_text2;
        }
    }

    public final int getSecondaryColor() {
        switch (phaseName) {
            case "Twilight Astronomical":
                return R.color.twilight_text;
            case "Twilight Nautical":
                return R.color.twilight_text;
            case "Twilight Civil":
                return R.color.twilight_text;
            case "Sunrise":
                return R.color.sunrise_text;
            case "Daylight":
                return R.color.daylight_text;
            case "Golden Hour":
                return R.color.daylight_text;
            case "Sunset":
                return R.color.sunset_text;
            default:
                return R.color.night_text;
        }
    }
}