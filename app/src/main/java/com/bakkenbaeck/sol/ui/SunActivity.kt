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

    private var sunsetBroadcastReceiver: SunsetBroadcastReceiver? = null
    private var timeReceiver: TimeReceiver? = null
    private var firstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sun)

        init()
        checkForLocationPermission()
    }

    private fun init() {
        this.sunsetBroadcastReceiver = SunsetBroadcastReceiver()
        val loadedTypeface = TypefaceUtils.load(assets, FONT_PATH)
        sunView.setTypeface(loadedTypeface)
        registerTimeReceiver()
    }

    private fun checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        } else {
            startLocationService()
        }
    }

    private fun registerTimeReceiver() {
        this.timeReceiver = TimeReceiver()
        this.registerReceiver(this.timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        startLocationService()
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, SunsetService::class.java)
        startService(serviceIntent)

        val intentFilter = IntentFilter(SunsetService.ACTION_UPDATE)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(this.sunsetBroadcastReceiver, intentFilter)
    }

    private inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val uvd = UserVisibleData(context, intent)
            updateView(uvd)
        }
    }

    private fun updateView(uvd: UserVisibleData) {
        val color = uvd.currentPhase.backgroundColor
        val secColor = uvd.currentPhase.secondaryColor
        val priColor = uvd.currentPhase.primaryColor

        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())

        if (this.firstTime) {
            todaysMessage.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

            sunView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()

            location.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()

            share.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
        }

        todaysMessage.text = uvd.todaysMessage
        todaysMessage.setTextColor(ContextCompat.getColor(this, secColor))
        location.setTextColor(ContextCompat.getColor(this, secColor))

        share.setTextColor(ContextCompat.getColor(this, secColor))
        location.text = uvd.locationMessage

        sunView.setColor(ContextCompat.getColor(this, priColor))
        sunView.setStartLabel(uvd.sunriseText)
        sunView.setEndLabel(uvd.sunsetText)
        val title = title as? TextView ?: return
        title.setTextColor(ContextCompat.getColor(this, secColor))
        sunCircle.setColorFilter(ContextCompat.getColor(this, secColor), PorterDuff.Mode.SRC)
        sunView.setFloatingLabel(sdf.format(Date()))

        val colorFrom = (activitySun.getBackground() as ColorDrawable).color
        val colorTo = ContextCompat.getColor(this, color)
        animateBackground(colorFrom, colorTo)

        titleWrapper.setOnClickListener { showInfoActivity(uvd.currentPhase.name) }

        sunView.setColor(ContextCompat.getColor(this, priColor))
                .setStartLabel(uvd.sunriseText)
                .setEndLabel(uvd.sunsetText)
                .setFloatingLabel(sdf.format(Date()))
                .setPercentProgress(uvd.progress)

        firstTime = false
    }

    private fun showInfoActivity(phaseName: String) {
        val intent = Intent(this, InfoActivity::class.java)
        intent.putExtra(InfoActivity.PHASE_NAME, phaseName)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) {
            return
        }

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 400
        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            activitySun.setBackgroundColor(color)
        }
        colorAnimation.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (sunsetBroadcastReceiver != null) {
            this.unregisterReceiver(sunsetBroadcastReceiver)
        }

        if (timeReceiver != null) {
            this.unregisterReceiver(timeReceiver)
        }
    }
}

