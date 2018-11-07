package com.bakkenbaeck.sol.util


import android.content.Context
import android.content.Intent
import android.text.Spanned
import com.bakkenbaeck.sol.BaseApplication

import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.calculateProgress
import com.bakkenbaeck.sol.extension.toHtml
import com.bakkenbaeck.sol.model.local.Phase

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UserVisibleData(context: Context, intent: Intent) {

    val todaysMessage: Spanned
    val locationMessage: String
    val currentPhase: Phase
    val sunriseText: String
    val sunsetText: String
    val progress: Double

    init {
        val dailyMessage = intent.getStringExtra(BaseApplication.EXTRA_DAILY_MESSAGE)
        todaysMessage = dailyMessage.toHtml()

        val sunriseTime = intent.getLongExtra(BaseApplication.EXTRA_SUNRISE_TIME, 0)
        val sunsetTime = intent.getLongExtra(BaseApplication.EXTRA_SUNSET_TIME, 0)
        locationMessage = intent.getStringExtra(BaseApplication.EXTRA_LOCATION_MESSAGE)
        currentPhase = Phase(intent.getStringExtra(BaseApplication.EXTRA_CURRENT_PHASE))

        val sdf = SimpleDateFormat(context.getString(R.string.hh_mm), Locale.getDefault())
        sunriseText = sdf.format(sunriseTime)
        sunsetText = sdf.format(sunsetTime)

        val currentTime = Calendar.getInstance().timeInMillis
        progress = Pair(sunriseTime, sunsetTime).calculateProgress(currentTime)
    }
}
