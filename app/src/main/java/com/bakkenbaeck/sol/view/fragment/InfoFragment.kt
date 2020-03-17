package com.bakkenbaeck.sol.view.fragment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.getColor
import com.bakkenbaeck.sol.extension.setTextToOnOrOff
import com.bakkenbaeck.sol.extension.toHtml
import com.bakkenbaeck.sol.model.local.Phase
import com.bakkenbaeck.sol.viewModel.InfoViewModel
import kotlinx.android.synthetic.main.fragment_info.*

class InfoFragment : Fragment() {

    companion object {
        private const val PHASE_NAME = "phaseName"

        private val color2drawable = mapOf(
            R.color.sunrise_text to R.drawable.half_circle_sunrise,
            R.color.daylight_text to R.drawable.half_circle_daylight,
            R.color.sunset_text to R.drawable.half_circle_daylight,
            R.color.twilight_text to R.drawable.half_circle_twilight,
            R.color.night_text to R.drawable.half_circle_night
        )
    }

    private val viewModel: InfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initBackground()
        initClickListeners()
        initObservers()
    }

    private fun initBackground() {
        viewModel.animateBackground.value = false
    }

    private fun initObservers() {
        viewModel.notifications.observe(viewLifecycleOwner, Observer {
            if (it != null) notificationValue.setTextToOnOrOff(it)
        })
        viewModel.animateBackground.observe(viewLifecycleOwner, Observer {
            if (it != null) setColorsFromPhaseName(it)
        })
    }

    private fun setColorsFromPhaseName(animateBackground: Boolean = false) {
        val phaseName = arguments?.getString(PHASE_NAME) ?: return
        val currentPhase = Phase(phaseName)

        val primaryColor = getColor(currentPhase.primaryColor)
        val secondaryColor = getColor(currentPhase.secondaryColor)

        infoMessage.movementMethod = LinkMovementMethod.getInstance()
        infoMessage.text = createInfoMessage(primaryColor)

        title?.setTextColor(secondaryColor)
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
        val colorTo = getColor(currentPhase.backgroundColor)
        if (animate) {
            animateBackground(colorFrom, colorTo)
        } else {
            root.setBackgroundColor(colorTo)
        }
        setSunDrawable(currentPhase)
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) return

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 400
            addUpdateListener { root.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    private fun setSunDrawable(currentPhase: Phase) {
        val color = currentPhase.secondaryColor
        val drawable = color2drawable[color] ?: return
        setSunDrawable(drawable)
    }

    private fun setSunDrawable(resourceId: Int) {
        val context = context ?: return
        val drawable = ContextCompat.getDrawable(context, resourceId)
        sunCircle.setImageDrawable(drawable)
    }

    private fun initClickListeners() {
        notificationWrapper.setOnClickListener { viewModel.toggleNotifications() }
        titleWrapper.setOnClickListener {
            val controller = findNavController()
            controller.navigateUp()
        }
    }
}