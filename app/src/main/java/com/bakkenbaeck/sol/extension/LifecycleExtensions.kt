package com.bakkenbaeck.sol.extension

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

inline fun <reified T> LifecycleOwner.observe(field: LiveData<T>,
                                              crossinline callback: (T) -> Unit) {
    field.observe(this, Observer {
        if (it != null) callback(it)
    })
}