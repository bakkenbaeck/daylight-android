package com.bakkenbaeck.sol.view.fragment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.animate
import com.bakkenbaeck.sol.extension.calculateProgress
import com.bakkenbaeck.sol.extension.getColor
import com.bakkenbaeck.sol.model.local.Phase
import com.bakkenbaeck.sol.viewModel.SunViewModel
import kotlinx.android.synthetic.main.fragment_sun.*
import java.text.SimpleDateFormat
import java.util.*

class SunFragment : Fragment() {

    private val viewModel: SunViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sun, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initObservers()
        initListeners()
    }

    private fun initObservers() {
        viewModel.todaysMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) todaysMessage.text = it
        })
        viewModel.locationMessage.observe(viewLifecycleOwner, Observer {
            if (it != null) location.text = it
        })
        viewModel.sunrise.observe(viewLifecycleOwner, Observer {
            if (it != null) updateSunriseText(it)
        })
        viewModel.sunset.observe(viewLifecycleOwner, Observer {
            if (it != null) updateSunsetText(it)
        })
        viewModel.progress.observe(viewLifecycleOwner, Observer {
            if (it != null) sunView.setPercentProgress(it)
        })
        viewModel.currentPhase.observe(viewLifecycleOwner, Observer {
            if (it != null) updateColors(it)
        })
        viewModel.date.observe(viewLifecycleOwner, Observer {
            if (it != null) updateDate(it)
        })
        viewModel.shouldAnimate.observe(viewLifecycleOwner, Observer {
            if (it == true) animateViews()
        })
    }

    private fun updateSunriseText(sunriseText: Long) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setStartLabel(sdf.format(sunriseText))
    }

    private fun updateSunsetText(sunsetText: Long) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setEndLabel(sdf.format(sunsetText))
    }

    private fun updateColors(phase: Phase) {
        updatePrimaryColor(phase.primaryColor)
        updateSecondaryColor(phase.secondaryColor)
        updateBackgroundColor(phase.backgroundColor)
    }

    private fun updatePrimaryColor(reference: Int) {
        val primaryColor = getColor(reference)

        todaysMessage.setTextColor(primaryColor)
        sunView.setColor(primaryColor)
    }

    private fun updateSecondaryColor(reference: Int) {
        val secondaryColor = getColor(reference)

        location.setTextColor(secondaryColor)
        share.setTextColor(secondaryColor)
        title?.setTextColor(secondaryColor)
        sunCircle.setColorFilter(secondaryColor, PorterDuff.Mode.SRC)
    }

    private fun updateBackgroundColor(reference: Int) {
        val backgroundColor = getColor(reference)
        val background = activitySun.background as? ColorDrawable ?: return
        animateBackground(background.color, backgroundColor)
    }

    private fun animateBackground(colorFrom: Int, colorTo: Int) {
        if (colorFrom == colorTo) return

        ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
            duration = 400
            addUpdateListener { activitySun.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
    }

    private fun updateDate(date: Date) {
        val sdf = SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault())
        sunView.setFloatingLabel(sdf.format(date))
    }

    private fun animateViews() {
        todaysMessage.animate(duration = 200)
        location.animate(duration = 200)
        sunView.animate(duration = 300)
        share.animate(duration = 200)

        viewModel.shouldAnimate.value = false
    }

    private fun initListeners() {
        titleWrapper.setOnClickListener { showInfoFragment() }

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

    private fun showInfoFragment() {
        val phaseName = viewModel.currentPhase.value?.name ?: return
        val directions = SunFragmentDirections.sunToInfo(phaseName)
        val controller = findNavController()
        controller.navigate(directions)
    }
}