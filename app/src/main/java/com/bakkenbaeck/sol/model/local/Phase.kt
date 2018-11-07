package com.bakkenbaeck.sol.model.local

import com.bakkenbaeck.sol.R
import com.florianmski.suncalc.models.SunPhase

class Phase(val name: String) {

    companion object {
        val backgroundMap = mapOf(
            "Twilight Astronomical" to R.color.twilight_astronomical,
            "Twilight Nautical" to R.color.twilight_nautical,
            "Twilight Civil" to R.color.twilight_civil,
            "Sunrise" to R.color.sunrise,
            "Daylight" to R.color.daylight,
            "Golden Hour" to R.color.golden_hour,
            "Sunset" to R.color.sunset
        )

        val primaryColorMap = mapOf(
            "Twilight Astronomical" to R.color.twilight_text2,
            "Twilight Nautical" to R.color.twilight_text2,
            "Twilight Civil" to R.color.twilight_text2,
            "Sunrise" to R.color.sunrise_text2,
            "Daylight" to R.color.daylight_text2,
            "Golden Hour" to R.color.daylight_text2,
            "Sunset" to R.color.sunset_text2
        )

        val secondaryColorMap = mapOf(
            "Twilight Astronomical" to R.color.twilight_text,
            "Twilight Nautical" to R.color.twilight_text,
            "Twilight Civil" to R.color.twilight_text,
            "Sunrise" to R.color.sunrise_text,
            "Daylight" to R.color.daylight_text,
            "Golden Hour" to R.color.daylight_text,
            "Sunset" to R.color.sunset_text
        )
    }

    val backgroundColor: Int by lazy { backgroundMap[name] ?:R.color.night }

    val primaryColor: Int by lazy { primaryColorMap[name] ?:R.color.night_text2 }

    val secondaryColor: Int by lazy { secondaryColorMap[name] ?: R.color.night_text }

    constructor(phase: SunPhase) : this(phase.name.toString())
}