package com.bakkenbaeck.sol.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            final Intent service = new Intent(context, SunsetService.class);
            service.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, false);
            context.startService(service);
        }
    }
}