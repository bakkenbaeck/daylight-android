package com.bakkenbaeck.sol.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.isLocationPermissionGranted
import com.bakkenbaeck.sol.service.TimeReceiver
import com.bakkenbaeck.sol.view.fragment.StartFragmentDirections
import com.bakkenbaeck.sol.viewModel.SunViewModel

class MainActivity : BaseActivity() {

    private val viewModel: SunViewModel by viewModels()
    private val sunsetBroadcastReceiver by lazy { SunsetBroadcastReceiver() }
    private val timeReceiver by lazy { TimeReceiver() }

    companion object {
        private const val PERMISSION_REQUEST_CODE_LOCATION = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        checkForLocationPermission()
        initBroadcastReceivers()
    }

    private fun checkForLocationPermission() {
        if (isLocationPermissionGranted()) {
            navigateToSunFragment()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION && isLocationPermissionGranted()) {
            navigateToSunFragment()
        }
    }

    private fun navigateToSunFragment() {
        val findNavController = findNavController(R.id.nav_host_fragment)
        val directions = StartFragmentDirections.startToSun()
        findNavController.navigate(directions)
    }

    private fun initBroadcastReceivers() {
        val intentFilter = IntentFilter(BaseApplication.ACTION_UPDATE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        registerReceiver(sunsetBroadcastReceiver, intentFilter)
        registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.updateLiveData(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(sunsetBroadcastReceiver)
        unregisterReceiver(timeReceiver)
    }
}