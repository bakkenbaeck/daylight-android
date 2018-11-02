package com.bakkenbaeck.sol

import android.app.Application

import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initCalligraphy()
    }

    private fun initCalligraphy() {
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/gtamericalight.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
    }
}
