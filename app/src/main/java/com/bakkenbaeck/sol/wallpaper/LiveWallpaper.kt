package com.bakkenbaeck.sol.wallpaper


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.bakkenbaeck.sol.BaseApplication

import com.bakkenbaeck.sol.util.UserVisibleData

class LiveWallpaper : WallpaperService() {
    override fun onCreateEngine(): WallpaperService.Engine {
        return SunWallpaper(this)
    }

    inner class SunWallpaper(private val context: Context) : WallpaperService.Engine() {
        private val handler = Handler()
        private val updateWallpaper = Runnable { updateWallpaper() }
        private var visible = true

        init {
            handler.post(updateWallpaper)
        }

        override fun onVisibilityChanged(visibility: Boolean) {
            visible = visibility
            if (visibility) {
                handler.post(updateWallpaper)
            } else {
                handler.removeCallbacks(updateWallpaper)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(updateWallpaper)
        }

        private fun updateWallpaper() {
            registerForSunPhaseChanges()
            BaseApplication.instance.refreshLocation(false)
        }

        private fun registerForSunPhaseChanges() {
            val receiver = SunsetBroadcastReceiver(this)
            val intentFilter = IntentFilter(BaseApplication.ACTION_UPDATE)
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
            applicationContext.registerReceiver(receiver, intentFilter)
        }

        private fun draw(uvd: UserVisibleData) {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.drawColor(getBackgroundColour(uvd))
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
            handler.removeCallbacks(updateWallpaper)
            if (visible) {
                handler.postDelayed(updateWallpaper, (1000 * 60 * 60).toLong())
            }
        }

        private fun getBackgroundColour(uvd: UserVisibleData): Int {
            return ContextCompat.getColor(context, uvd.currentPhase.backgroundColor)
        }

        private inner class SunsetBroadcastReceiver(private val wallpaper: SunWallpaper) : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val uvd = UserVisibleData(context, intent)
                this.wallpaper.draw(uvd)
            }
        }
    }
}
