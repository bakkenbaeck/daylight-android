package com.bakkenbaeck.sol.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, SunsetService::class.java)
        context.startService(service)
    }
}
