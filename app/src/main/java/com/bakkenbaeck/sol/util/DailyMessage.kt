package com.bakkenbaeck.sol.util

import android.content.Context
import android.support.v4.content.ContextCompat

import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.location.CurrentCity
import com.florianmski.suncalc.models.SunPhase

import java.util.Calendar

class DailyMessage(private val context: Context) {

    private val currentCity: CurrentCity = CurrentCity(context)

    fun generate(threeDayPhases: ThreeDayPhases): String {
        val phase = threeDayPhases.currentPhase
        val phaseName = phase.name

        val isNight = isNight(phaseName)
        val dayLengthChange = if (isNight)
            threeDayPhases.dayLengthChangeBetweenTodayAndTomorrow
        else
            threeDayPhases.dayLengthChangeBetweenTodayAndYesterday

        val isDayGettingLonger = isDayGettingLonger(dayLengthChange)
        val primaryColor = phase.primaryColor
        val minutes = getRoundedMinutesForCalendar(dayLengthChange)

        return generateMessage(phaseName, minutes, isDayGettingLonger, primaryColor)
    }

    private fun generateMessage(phaseName: String,
                                minutes: Int,
                                isDayGettingLonger: Boolean,
                                primaryColor: Int): String {

        val messageArray = getMessageArray(minutes, isDayGettingLonger, phaseName)

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val messagePosition = dayOfYear % messageArray.size

        val color = ContextCompat.getColor(context, primaryColor)

        return messageArray[messagePosition]
                .replace("{color}", color.toString())
                .replace("{numMinutes}", minutes.toString())
                .replace("{minutes}", plural(minutes))
    }

    private fun getMessageArray(minutes: Int, isDayGettingLonger: Boolean, phaseName: String): Array<String> {
        val isNight = isNight(phaseName)
        val isLessThanAMinute = minutes == 0
        val messageArrayRef: Int

        if (isLessThanAMinute) {
            messageArrayRef = if (isNight) {
                if (isDayGettingLonger)
                    R.array.night_messages_positive_less_than_one_minute
                else
                    R.array.night_messages_negative_less_than_one_minute
            } else {
                if (isDayGettingLonger)
                    R.array.day_messages_positive_less_than_one_minute
                else
                    R.array.day_messages_negative_less_than_one_minute
            }
        } else {
            messageArrayRef = if (isNight) {
                if (isDayGettingLonger)
                    R.array.night_messages_positive
                else
                    R.array.night_messages_negative
            } else {
                if (isDayGettingLonger)
                    R.array.day_messages_positive
                else
                    R.array.day_messages_negative
            }
        }

        return context.resources.getStringArray(messageArrayRef)
    }

    private fun isNight(phaseName: String): Boolean {
        val nightPhaseName = SunPhase.all()[ThreeDayPhases.NIGHT].name.toString()
        return phaseName == nightPhaseName
    }

    private fun plural(minutes: Int): String {
        return if (minutes > 1)
            context.resources.getString(R.string.minutes)
        else
            context.resources.getString(R.string.minute)
    }

    fun getLocation(latitude: Double, longitude: Double): String {
        return currentCity.getCityAndCountry(latitude, longitude)
    }

    private fun isDayGettingLonger(dayLengthChange: Calendar): Boolean {
        val dayLengthChangeInSeconds = dayLengthChange.timeInMillis / 1000
        return dayLengthChangeInSeconds > 0
    }

    private fun getRoundedMinutesForCalendar(dayLengthChange: Calendar): Int {
        val numMinutes = Math.abs(dayLengthChange.timeInMillis.toDouble() / (1000 * 60).toDouble())
        return Math.round(numMinutes).toInt()
    }
}
