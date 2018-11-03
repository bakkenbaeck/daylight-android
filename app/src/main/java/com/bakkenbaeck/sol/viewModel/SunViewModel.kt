package com.bakkenbaeck.sol.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.service.SunsetService
import com.bakkenbaeck.sol.service.TimeReceiver
import com.bakkenbaeck.sol.util.UserVisibleData

class SunViewModel : ViewModel() {

    val userVisibleData by lazy { MutableLiveData<UserVisibleData>() }
    val shouldAnimate by lazy { MutableLiveData<Boolean>() }

    private val sunsetBroadcastReceiver = SunsetBroadcastReceiver()
    private val timeReceiver = TimeReceiver()

    init {
        BaseApplication.instance.apply {
            registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        }
        shouldAnimate.postValue(true)
    }

    fun startLocationService() {
        BaseApplication.instance.apply {
            val serviceIntent = Intent(this, SunsetService::class.java)
            startService(serviceIntent)

            val intentFilter = IntentFilter(SunsetService.ACTION_UPDATE)
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            registerReceiver(sunsetBroadcastReceiver, intentFilter)
        }
    }

    private inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            userVisibleData.postValue(UserVisibleData(context, intent))
        }
    }

    override fun onCleared() {
        super.onCleared()
        BaseApplication.instance.apply {
            unregisterReceiver(sunsetBroadcastReceiver)
            unregisterReceiver(timeReceiver)
        }
    }
}