package com.bakkenbaeck.sol.service


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, SunsetService::class.java)
        service.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, true)
        context.startService(service)
    }
}
