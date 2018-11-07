package com.bakkenbaeck.sol.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.arch.lifecycle.Observer
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.finishWithTransition
import com.bakkenbaeck.sol.extension.getViewModel
import com.bakkenbaeck.sol.extension.toHtml
import com.bakkenbaeck.sol.extension.toOnOrOff
import com.bakkenbaeck.sol.model.local.Phase
import com.bakkenbaeck.sol.viewModel.InfoViewModel
import kotlinx.android.synthetic.main.activity_info.notificationText
import kotlinx.android.synthetic.main.activity_info.notificationValue
import kotlinx.android.synthetic.main.activity_info.root
import kotlinx.android.synthetic.main.activity_info.titleWrapper
import kotlinx.android.synthetic.main.activity_info.notificationWrapper
import kotlinx.android.synthetic.main.activity_info.infoMessage
import kotlinx.android.synthetic.main.activity_info.sunCircle

class InfoActivity : BaseActivity() {

    companion object {
        const val PHASE_NAME = "phase_name"

        private val color2drawable = mapOf(
                R.color.sunrise_text to R.drawable.half_circle_sunrise,
                R.color.daylight_text to R.drawable.half_circle_daylight,
                R.color.sunset_text to R.drawable.half_circle_daylight,
                R.color.twilight_text to R.drawable.half_circle_twilight,
                R.color.night_text to R.drawable.half_circle_night
        )
    }

    private lateinit var viewModel: InfoViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        init()
    }

    private fun init() {
        initViewModel()
        initBackground()
        initClickListeners()
        initObservers()
    }

    private fun initObservers() {
        viewModel.notifications.observe(this, Observer {
            if (it != null) notificationValue.text = toOnOrOff(it)
        })
        viewModel.animateBackground.observe(this, Observer {
            if (it != null) setColorsFromPhaseName(it)
        })
    }

    private fun initViewModel() {
        viewModel = getViewModel()
    }

    private fun initBackground() {
        viewModel.animateBackground.value = false
    }

    private fun setColorsFromPhaseName(animateBackground: Boolean = false) {
        val phaseName = intent.getStringExtra(PHASE_NAME)
        val currentPhase = Phase(phaseName)

        val primaryColor = ContextCompat.getColor(this, currentPhase.primaryColor)
        val secondaryColor = ContextCompat.getColor(this, currentPhase.secondaryColor)

        infoMessage.movementMethod = LinkMovementMethod.getInstance()
        infoMessage.text = createInfoMessage(primaryColor)

        (title as? TextView)?.setTextColor(secondaryColor)
        infoMessage.setTextColor(secondaryColor)
        notificationText.setTextColor(secondaryColor)
        notificationValue.setTextColor(primaryColor)

        initBackground(currentPhase, animateBackground)
    }

    private fun createInfoMessage(primaryColor: Int): Spanned {
        val message = resources.getString(R.string.info_message)
        val formattedInfo = message.replace("{color}", primaryColor.toString())
        return formattedInfo.toHtml()
    }

    private fun initBackground(currentPhase: Phase, animate: Boolean) {
        val colorFrom = (root.background as ColorDrawable).color
        val colorTo = ContextCompat.getColor(this, currentPhase.backgroundColor)
        if (animate) {
            animateBackground(colorFrom, colorTo)
        } else {
            root.setBackgroundColor(colorTo)
        }
        setSunDrawable(currentPhase)
    }

    private fun initClickListeners() {
        notificationWrapper.setOnClickListener { viewModel.toggleNotifications() }
        titleWrapper.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun setSunDrawable(currentPhase: Phase) {
        val color = currentPhase.secondaryColor
        val drawable = color2drawable[color] ?: return
        setSunDrawable(drawable)
    }

    private fun setSunDrawable(resourceId: Int) {
        val drawable = ContextCompat.getDrawable(this, resourceId)
        sunCircle.setImageDrawable(drawable)
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) return

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 400
            addUpdateListener { root.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithTransition()
    }
}
