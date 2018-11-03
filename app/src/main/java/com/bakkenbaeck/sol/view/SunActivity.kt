package com.bakkenbaeck.sol.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.arch.lifecycle.Observer
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.startActivityWithTransition
import com.bakkenbaeck.sol.extension.animate
import com.bakkenbaeck.sol.extension.getViewModel
import com.bakkenbaeck.sol.extension.requireLocationPermission
import com.bakkenbaeck.sol.util.UserVisibleData
import com.bakkenbaeck.sol.viewModel.SunViewModel
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

    private lateinit var viewModel: SunViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sun)

        init()
    }

    private fun init() {
        initViewModel()
        initTypeface()
        checkForLocationPermission()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initTypeface() {
        val loadedTypeface = TypefaceUtils.load(assets, FONT_PATH)
        sunView.setTypeface(loadedTypeface)
    }

    private fun checkForLocationPermission() {
        requireLocationPermission(PERMISSION_REQUEST_CODE) {
            viewModel.startLocationService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSION_REQUEST_CODE) viewModel.startLocationService()
    }
    private fun initObservers() {
        viewModel.userVisibleData.observe(this, Observer {
            if (it != null) updateView(it)
        })
        viewModel.shouldAnimate.observe(this, Observer {
            if (it != true) return@Observer

            todaysMessage.animate(duration = 200)
            location.animate(duration = 200)
            sunView.animate(duration = 300)
            share.animate(duration = 200)

            viewModel.shouldAnimate.value = false
        })
    }

    private fun updateView(uvd: UserVisibleData) {
        val primaryColor = ContextCompat.getColor(this, uvd.currentPhase.primaryColor)
        val secondaryColor = ContextCompat.getColor(this, uvd.currentPhase.secondaryColor)
        val backgroundColor = ContextCompat.getColor(this, uvd.currentPhase.backgroundColor)

        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())

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
}

