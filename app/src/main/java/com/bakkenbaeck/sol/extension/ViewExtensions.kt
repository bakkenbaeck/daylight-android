package com.bakkenbaeck.sol.extension

import android.view.View

fun View.animate(alpha: Float = 1f, duration: Long) {
    animate()
            .alpha(alpha)
            .setDuration(duration)
            .start()
}