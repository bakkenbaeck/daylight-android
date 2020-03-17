package com.bakkenbaeck.sol.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.*
import com.bakkenbaeck.sol.model.local.Phase
import com.bakkenbaeck.sol.service.TimeReceiver
import com.bakkenbaeck.sol.viewModel.SunViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.android.synthetic.main.activity_sun.activitySun
import kotlinx.android.synthetic.main.activity_sun.location
import kotlinx.android.synthetic.main.activity_sun.titleWrapper
import kotlinx.android.synthetic.main.activity_sun.todaysMessage
import kotlinx.android.synthetic.main.activity_sun.share
import kotlinx.android.synthetic.main.activity_sun.sunCircle
import kotlinx.android.synthetic.main.activity_sun.sunView

class SunActivity : BaseActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    private val viewModel: SunViewModel by viewModels()
    private val sunsetBroadcastReceiver by lazy { SunsetBroadcastReceiver() }
    private val timeReceiver by lazy { TimeReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sun)
        init()
    }

    private fun init() {
        checkForLocationPermission()
        initBroadcastReceivers()
        initObservers()
        initListeners()
    }

    private fun checkForLocationPermission() {
        requireLocationPermission(PERMISSION_REQUEST_CODE) {
            refreshLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSION_REQUEST_CODE) refreshLocation()
    }

    private fun refreshLocation() {
        BaseApplication.instance.refreshLocation(false)
    }

    private fun initBroadcastReceivers() {
        val intentFilter = IntentFilter(BaseApplication.ACTION_UPDATE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        registerReceiver(sunsetBroadcastReceiver, intentFilter)
        registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun initObservers() {
        viewModel.todaysMessage.observe(this, Observer {
            if (it != null) todaysMessage.text = it
        })
        viewModel.locationMessage.observe(this, Observer {
            if (it != null) location.text = it
        })
        viewModel.sunrise.observe(this, Observer {
            if (it != null) updateSunriseText(it)
        })
        viewModel.sunset.observe(this, Observer {
            if (it != null) updateSunsetText(it)
        })
        viewModel.progress.observe(this, Observer {
            if (it != null) sunView.setPercentProgress(it)
        })
        viewModel.currentPhase.observe(this, Observer {
            if (it != null) updateColors(it)
        })
        viewModel.date.observe(this, Observer {
            if (it != null) updateDate(it)
        })
        viewModel.shouldAnimate.observe(this, Observer {
            if (it == true) animateViews()
        })
    }

    private fun animateViews() {
        todaysMessage.animate(duration = 200)
        location.animate(duration = 200)
        sunView.animate(duration = 300)
        share.animate(duration = 200)

        viewModel.shouldAnimate.value = false
    }

    private fun updateColors(phase: Phase) {
        updatePrimaryColor(phase.primaryColor)
        updateSecondaryColor(phase.secondaryColor)
        updateBackgroundColor(phase.backgroundColor)
    }

    private fun updateSunriseText(sunriseText: Long) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setStartLabel(sdf.format(sunriseText))
    }

    private fun updateSunsetText(sunsetText: Long) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setEndLabel(sdf.format(sunsetText))
    }

    private fun updatePrimaryColor(reference: Int) {
        val primaryColor = ContextCompat.getColor(this, reference)

        todaysMessage.setTextColor(primaryColor)
        sunView.setColor(primaryColor)
    }

    private fun updateSecondaryColor(reference: Int) {
        val secondaryColor = ContextCompat.getColor(this, reference)

        location.setTextColor(secondaryColor)
        share.setTextColor(secondaryColor)
        (title as? TextView)?.setTextColor(secondaryColor)
        sunCircle.setColorFilter(secondaryColor, PorterDuff.Mode.SRC)
    }

    private fun updateBackgroundColor(reference: Int) {
        val backgroundColor = ContextCompat.getColor(this, reference)
        val background = activitySun.background as? ColorDrawable ?: return
        animateBackground(background.color, backgroundColor)
    }

    private fun updateDate(date: Date) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setFloatingLabel(sdf.format(date))
    }

    private fun initListeners() {
        titleWrapper.setOnClickListener { showInfoActivity() }

        // TODO Remove this listener
        todaysMessage.setOnClickListener {
            val oldDate = viewModel.date.value ?: return@setOnClickListener
            val newTime = oldDate.time - (1000*60*60)
            viewModel.date.postValue(Date(newTime))

            val sunriseTime = viewModel.sunrise.value ?: return@setOnClickListener
            val sunsetTime = viewModel.sunset.value ?: return@setOnClickListener
            val progress = Pair(sunriseTime, sunsetTime).calculateProgress(newTime)
            viewModel.progress.postValue(progress)
        }
    }

    private fun showInfoActivity() {
        val phaseName = viewModel.currentPhase.value?.name ?: return
        startActivityWithTransition<InfoActivity> {
            putExtra(InfoActivity.PHASE_NAME, phaseName)
        }
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) return

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 400
            addUpdateListener { activitySun.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.updateLiveData(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sunsetBroadcastReceiver)
        unregisterReceiver(timeReceiver)
    }
}

