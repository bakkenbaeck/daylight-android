package com.bakkenbaeck.sol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent service = new Intent(context, SunsetService.class);
        context.startService(service);
    }
}
