package com.bakkenbaeck.sol.extension

import android.view.View
import android.widget.TextView
import com.bakkenbaeck.sol.R

fun View.animate(alpha: Float = 1f, duration: Long) {
    animate()
            .alpha(alpha)
            .setDuration(duration)
            .start()
}

fun TextView.setTextToOnOrOff(value: Boolean) {
    val resourceId = if (value) R.string.off else R.string.on
    text = resources.getString(resourceId)
}