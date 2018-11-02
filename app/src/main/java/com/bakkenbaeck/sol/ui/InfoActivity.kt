package com.bakkenbaeck.sol.ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.service.SunsetService
import com.bakkenbaeck.sol.util.CurrentPhase
import com.bakkenbaeck.sol.util.SolPreferences
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
    }

    private var sunsetBroadcastReceiver: SunsetBroadcastReceiver? = null
    private var notificationEnabled: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        init()
    }

    private fun init() {

        setColorsFromPhaseName(false)
        initNotificationsToggle()
        assignClickListeners()
        registerForSunPhaseChanges()
    }

    private fun setColorsFromPhaseName(animateBackground: Boolean) {
        val phaseName = intent.getStringExtra(PHASE_NAME)

        val currentPhase = CurrentPhase(phaseName)
        val message = resources.getString(R.string.info_message)
        val formattedInfo = message.replace("{color}", ContextCompat
                .getColor(this, currentPhase.primaryColor).toString())
        val s = convertToHtml(formattedInfo)

        infoMessage.movementMethod = LinkMovementMethod.getInstance()
        infoMessage.text = s

        val color = currentPhase.backgroundColor
        val priColor = currentPhase.primaryColor
        val secColor = currentPhase.secondaryColor

        val title = title  as? TextView ?: return
        title.setTextColor(ContextCompat.getColor(this, secColor))
        infoMessage.setTextColor(ContextCompat.getColor(this, secColor))
        notificationText.setTextColor(ContextCompat.getColor(this, secColor))
        notificationValue.setTextColor(ContextCompat.getColor(this, priColor))

        val colorFrom = (root.getBackground() as ColorDrawable).color
        val colorTo = ContextCompat.getColor(this, color)

        if (animateBackground) {
            animateBackground(colorFrom, colorTo)
        } else {
            root.setBackgroundColor(colorTo)
        }

        setSunDrawable(currentPhase)
    }

    private fun initNotificationsToggle() {
        val solPrefs = SolPreferences(this)
        this.notificationEnabled = solPrefs.showNotification

        val s = if (notificationEnabled) getString(R.string.off) else getString(R.string.on)
        notificationValue.setText(s)
    }

    private fun assignClickListeners() {
        notificationWrapper.setOnClickListener {
            notificationEnabled = !notificationEnabled
            val s = if (notificationEnabled) getString(R.string.off) else getString(R.string.on)
            notificationValue.setText(s)

            val solPrefs = SolPreferences(this@InfoActivity)
            solPrefs.showNotification = notificationEnabled
        }

        titleWrapper.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun registerForSunPhaseChanges() {
        val intentFilter = IntentFilter(SunsetService.ACTION_UPDATE)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        this.sunsetBroadcastReceiver = SunsetBroadcastReceiver()
        registerReceiver(this.sunsetBroadcastReceiver, intentFilter)
    }

    private inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setColorsFromPhaseName(true)
        }
    }

    private fun setSunDrawable(currentPhase: CurrentPhase) {
        val color = currentPhase.secondaryColor

        when (color) {
            R.color.sunrise_text -> {
                sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_sunrise))
            }
            R.color.daylight_text -> {
                sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_daylight))
            }
            R.color.sunset_text -> {
                sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_daylight))
            }
            R.color.twilight_text -> {
                sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_twilight))
            }
            R.color.night_text -> {
                sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_night))
            }
        }
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) {
            return
        }

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 400
        colorAnimation.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            root.setBackgroundColor(color)
        }
        colorAnimation.start()
    }

    private fun convertToHtml(message: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(message)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (sunsetBroadcastReceiver != null) {
            unregisterReceiver(sunsetBroadcastReceiver)
            sunsetBroadcastReceiver = null
        }
    }
}
