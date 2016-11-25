package com.bakkenbaeck.sol;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
