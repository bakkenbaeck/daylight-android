package com.bakkenbaeck.sol.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Intent service = new Intent(context, SunsetService.class);
        service.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, true);
        context.startService(service);
    }
}
