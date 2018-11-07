package com.bakkenbaeck.sol.util


import android.location.Location
import com.bakkenbaeck.sol.model.local.Phase

import com.florianmski.suncalc.SunCalc
import com.florianmski.suncalc.models.SunPhase

import java.util.Calendar
import java.util.Date

class ThreeDayPhases(location: Location) {

    companion object {
        private const val SUNRISE = 4
        private const val SUNSET = 8
        const val NIGHT = 12
    }

    private val today: Calendar by lazy { Calendar.getInstance() }
    private val tomorrow: Calendar by lazy { createCalendarWithDayOffset(1) }
    private val yesterday: Calendar by lazy { createCalendarWithDayOffset(-1) }

    private val yesterdaysSunPhases: List<SunPhase>
    private val todaysSunPhases: List<SunPhase>
    private val tomorrowsSunPhases: List<SunPhase>

    init {
        val lat = location.latitude
        val lon = location.longitude

        yesterdaysSunPhases = SunCalc.getPhases(yesterday, lat, lon)
        todaysSunPhases = SunCalc.getPhases(today, lat, lon)
        tomorrowsSunPhases = SunCalc.getPhases(tomorrow, lat, lon)
    }

    val dayLengthChangeBetweenTodayAndYesterday: Calendar
        get() {
            val todayLength = getDayLengthForPhases(todaysSunPhases)
            val yesterdayLength = getDayLengthForPhases(yesterdaysSunPhases)

            val c = Calendar.getInstance()
            c.timeInMillis = todayLength.timeInMillis - yesterdayLength.timeInMillis

            return c
        }

    val dayLengthChangeBetweenTodayAndTomorrow: Calendar
        get() {
            val todayLength = getDayLengthForPhases(todaysSunPhases)
            val tomorrowLength = getDayLengthForPhases(tomorrowsSunPhases)

            val c = Calendar.getInstance()
            c.timeInMillis = tomorrowLength.timeInMillis - todayLength.timeInMillis

            return c
        }

    val tomorrowsSunrise: Long
        get() {
            val sunrise = tomorrowsSunPhases[SUNRISE]
            return sunrise.startDate.time.time
        }

    val todaysSunriseAsLong: Long
        get() {
            val sunrise = todaysSunPhases[SUNRISE]
            return sunrise.startDate.timeInMillis
        }

    val todaysSunsetAsLong: Long
        get() {
            val sunset = todaysSunPhases[SUNSET]
            return sunset.endDate.timeInMillis
        }

    val currentPhase: Phase
        get() {
            val now = Calendar.getInstance()
            for (phase in todaysSunPhases) {
                if (phase.endDate.after(now)) {
                    return Phase(phase)
                }
            }
            return Phase(todaysSunPhases[0])
        }

    private fun createCalendarWithDayOffset(dayOffset: Int): Calendar {
        return Calendar.getInstance().apply { add(Calendar.DATE, dayOffset) }
    }

    private fun getDayLengthForPhases(sunPhases: List<SunPhase>): Calendar {
        val sunrise = sunPhases[SUNRISE]
        val sunset = sunPhases[SUNSET]

        val sunriseTime = sunrise.startDate.time
        val sunsetTime = sunset.endDate.time
        val dayLengthInMillis = sunsetTime.time - sunriseTime.time

        val c = Calendar.getInstance()
        c.timeInMillis = dayLengthInMillis

        return c
    }
}
