package com.bakkenbaeck.sol.viewModel

import android.content.Intent
import android.text.Spanned
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.extension.calculateProgress
import com.bakkenbaeck.sol.extension.toHtml
import com.bakkenbaeck.sol.model.local.Phase
import java.util.Calendar
import java.util.Date

class SunViewModel : ViewModel() {

    val todaysMessage by lazy { MutableLiveData<Spanned>() }
    val locationMessage by lazy { MutableLiveData<String>() }
    val currentPhase by lazy { MutableLiveData<Phase>() }
    val sunrise by lazy { MutableLiveData<Long>() }
    val sunset by lazy { MutableLiveData<Long>() }
    val progress by lazy { MutableLiveData<Double>() }
    val date by lazy { MutableLiveData<Date>() }

    val shouldAnimate by lazy { MutableLiveData<Boolean>() }

    init {
        val baseApplication = BaseApplication.instance
        baseApplication.refreshLocation(false)

        shouldAnimate.postValue(true)
        date.value = Date()
    }

    fun updateLiveData(intent: Intent) {
        todaysMessage.value = intent.getStringExtra(BaseApplication.EXTRA_DAILY_MESSAGE).toHtml()
        locationMessage.value = intent.getStringExtra(BaseApplication.EXTRA_LOCATION_MESSAGE)
        currentPhase.value = Phase(intent.getStringExtra(BaseApplication.EXTRA_CURRENT_PHASE))

        val sunriseTime = intent.getLongExtra(BaseApplication.EXTRA_SUNRISE_TIME, 0)
        val sunsetTime = intent.getLongExtra(BaseApplication.EXTRA_SUNSET_TIME, 0)
        sunrise.value = sunriseTime
        sunset.value = sunsetTime

        val currentTime = Calendar.getInstance().timeInMillis
        date.value = Date(currentTime)
        progress.value = Pair(sunriseTime, sunsetTime).calculateProgress(currentTime)
    }
}