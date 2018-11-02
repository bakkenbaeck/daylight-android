package com.bakkenbaeck.sol.util


import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.Spanned

import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.service.SunsetService

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserVisibleData(context: Context, intent: Intent) {

    val todaysMessage: Spanned
    val locationMessage: String
    val currentPhase: CurrentPhase
    val sunriseText: String
    val sunsetText: String
    val progress: Double

    init {
        val dailyMessage = intent.getStringExtra(SunsetService.EXTRA_DAILY_MESSAGE)
        this.todaysMessage = convertToHtml(dailyMessage)

        val sunriseTime = intent.getLongExtra(SunsetService.EXTRA_SUNRISE_TIME, 0)
        val sunsetTime = intent.getLongExtra(SunsetService.EXTRA_SUNSET_TIME, 0)
        this.locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE)
        this.currentPhase = CurrentPhase(intent.getStringExtra(SunsetService.EXTRA_CURRENT_PHASE))

        val sdf = SimpleDateFormat(context.getString(R.string.hh_mm), Locale.getDefault())
        this.sunriseText = sdf.format(sunriseTime)
        this.sunsetText = sdf.format(sunsetTime)

        this.progress = calculateProgress(sunriseTime, sunsetTime)
    }

    private fun calculateProgress(sunrise: Long, sunset: Long): Double {
        val span = sunset - sunrise
        val current = Calendar.getInstance().timeInMillis - sunrise
        return current.toDouble() / span.toDouble()
    }

    private fun convertToHtml(message: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(message)
        }
    }
}
