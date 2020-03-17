package com.bakkenbaeck.sol.extension

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.getColor(reference: Int): Int {
    val context = context ?: return Color.TRANSPARENT
    return ContextCompat.getColor(context, reference)
}