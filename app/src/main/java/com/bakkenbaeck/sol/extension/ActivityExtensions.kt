package com.bakkenbaeck.sol.extension

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.bakkenbaeck.sol.R


inline fun <reified T> AppCompatActivity.startActivityWithTransition(func: Intent.() -> Intent) {
    val intent = Intent(this, T::class.java).func()
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

inline fun <reified T> AppCompatActivity.startActivityAndFinish() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
    finishWithTransition()
}

fun AppCompatActivity.finishWithTransition() {
    finish()
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

fun AppCompatActivity.requireLocationPermission(requestCode: Int, callback: () -> Unit) {
    if (isLocationPermissionGranted()) {
        callback()
    } else {
        requestLocationPermission(requestCode)
    }
}

fun AppCompatActivity.requestLocationPermission(requestCode: Int) {
    val request = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    ActivityCompat.requestPermissions(this, request, requestCode)
}

inline fun <reified T : ViewModel> AppCompatActivity.getViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}