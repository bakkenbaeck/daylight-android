package com.bakkenbaeck.sol;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCalligraphy();
    }

    private void initCalligraphy() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/gtamericalight.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
