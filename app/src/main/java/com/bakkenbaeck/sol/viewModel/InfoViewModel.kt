package com.bakkenbaeck.sol.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bakkenbaeck.sol.BaseApplication
import com.bakkenbaeck.sol.service.SunsetService
import com.bakkenbaeck.sol.util.SolPreferences

class InfoViewModel : ViewModel() {

    val notifications by lazy { MutableLiveData<Boolean>() }
    val animateBackground by lazy { MutableLiveData<Boolean>() }

    private val solPrefs by lazy { SolPreferences(BaseApplication.instance) }
    private val sunsetBroadcastReceiver by lazy { SunsetBroadcastReceiver() }

    init {
        notifications.postValue(solPrefs.showNotification)
        registerForSunPhaseChanges()
    }

    private fun registerForSunPhaseChanges() {
        val intentFilter = IntentFilter(SunsetService.ACTION_UPDATE)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        BaseApplication.instance.registerReceiver(sunsetBroadcastReceiver, intentFilter)
    }

    fun toggleNotifications() {
        val value = notifications.value != true
        notifications.postValue(value)
        solPrefs.showNotification = value
    }

    private inner class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            animateBackground.postValue(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        BaseApplication.instance.unregisterReceiver(sunsetBroadcastReceiver)
    }
}
