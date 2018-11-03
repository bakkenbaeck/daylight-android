package com.bakkenbaeck.sol.extension

import android.Manifest
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.bakkenbaeck.sol.R

inline fun <reified T> AppCompatActivity.startActivityWithTransition(func: Intent.() -> Intent) {
    val intent = Intent(this, T::class.java).func()
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

inline fun <reified T> AppCompatActivity.startActivityAndFinish() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
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

fun AppCompatActivity.isLocationPermissionGranted(): Boolean {
    val locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    val getPermissionState = ActivityCompat.checkSelfPermission(this, locationPermission)
    return getPermissionState == PackageManager.PERMISSION_GRANTED
}

inline fun <reified T : ViewModel> AppCompatActivity.getViewModel(): T {
    return ViewModelProviders.of(this).get(T::class.java)
}