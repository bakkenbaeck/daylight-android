package com.bakkenbaeck.sol.ui

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.startActivityWithTransition
import com.bakkenbaeck.sol.extension.animate
import com.bakkenbaeck.sol.extension.isLocationPermissionGranted
import com.bakkenbaeck.sol.extension.requireLocationPermission
import com.bakkenbaeck.sol.service.SunsetService
import com.bakkenbaeck.sol.service.TimeReceiver
import com.bakkenbaeck.sol.util.UserVisibleData
import uk.co.chrisjenx.calligraphy.TypefaceUtils
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
        private const val FONT_PATH = "fonts/gtamericalight.ttf"
    }

    private val sunsetBroadcastReceiver = SunsetBroadcastReceiver()
    private val timeReceiver = TimeReceiver()
    private var firstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sun)

        init()
    }

    private fun init() {
        initTypeface()
        initTimeReceiver()
        checkForLocationPermission()
    }

    private fun initTypeface() {
        val loadedTypeface = TypefaceUtils.load(assets, FONT_PATH)
        sunView.setTypeface(loadedTypeface)
    }

    private fun initTimeReceiver() {
        this.registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    private fun checkForLocationPermission() {
        requireLocationPermission(PERMISSION_REQUEST_CODE) {
            startLocationService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            startLocationService()
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, SunsetService::class.java)
        startService(serviceIntent)

        val intentFilter = IntentFilter(SunsetService.ACTION_UPDATE)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(sunsetBroadcastReceiver, intentFilter)
    }

    private inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val uvd = UserVisibleData(context, intent)
            updateView(uvd)
        }
    }

    private fun updateView(uvd: UserVisibleData) {
        val primaryColor = ContextCompat.getColor(this, uvd.currentPhase.primaryColor)
        val secondaryColor = ContextCompat.getColor(this, uvd.currentPhase.secondaryColor)
        val backgroundColor = ContextCompat.getColor(this, uvd.currentPhase.backgroundColor)

        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())

        if (firstTime) {
            todaysMessage.animate(duration = 200)
            location.animate(duration = 200)
            sunView.animate(duration = 300)
            share.animate(duration = 200)
        }

        todaysMessage.apply {
            text = uvd.todaysMessage
            setTextColor(secondaryColor)
        }

        location.apply {
            text = uvd.locationMessage
            setTextColor(secondaryColor)
        }

        share.setTextColor(secondaryColor)

        sunView.apply {
            setColor(primaryColor)
            sunView.setStartLabel(uvd.sunriseText)
            sunView.setEndLabel(uvd.sunsetText)
        }

        (title as? TextView)?.setTextColor(secondaryColor)

        sunCircle.setColorFilter(secondaryColor, PorterDuff.Mode.SRC)
        sunView.setFloatingLabel(sdf.format(Date()))

        (activitySun.background as? ColorDrawable)?.color?.let {
            animateBackground(it, backgroundColor)
        }


        titleWrapper.setOnClickListener { showInfoActivity(uvd.currentPhase.name) }

        sunView.apply {
            setColor(primaryColor)
            setStartLabel(uvd.sunriseText)
            setEndLabel(uvd.sunsetText)
            setFloatingLabel(sdf.format(Date()))
            setPercentProgress(uvd.progress)
        }

        firstTime = false
    }

    private fun showInfoActivity(phaseName: String) {
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sunsetBroadcastReceiver)
        unregisterReceiver(timeReceiver)
    }
}

