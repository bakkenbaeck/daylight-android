package com.bakkenbaeck.sol.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bakkenbaeck.sol.BaseApplication

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            BaseApplication.instance.refreshLocation(false)
        }
    }
}