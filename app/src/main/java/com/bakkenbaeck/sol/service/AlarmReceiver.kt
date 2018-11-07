package com.bakkenbaeck.sol.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bakkenbaeck.sol.BaseApplication

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        BaseApplication.instance.refreshLocation(true)
    }
}
