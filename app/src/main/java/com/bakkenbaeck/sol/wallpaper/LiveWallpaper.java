package com.bakkenbaeck.sol.wallpaper;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;

import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.util.UserVisibleData;

public class LiveWallpaper extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new SunWallpaper(this);
    }

    public class SunWallpaper extends Engine {
        private final Handler handler = new Handler();
        private final Runnable updateWallpaper = new Runnable() {
            @Override
            public void run() {
                updateWallpaper();
            }
        };

        private final Context context;
        private boolean visible = true;

        public SunWallpaper(final Context context) {
            this.context = context;
            handler.post(updateWallpaper);
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
            this.visible = visible;
            if (visible) {
                this.handler.post(updateWallpaper);
            } else {
                this.handler.removeCallbacks(updateWallpaper);
            }
        }

        @Override
        public void onSurfaceDestroyed(final SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            this.handler.removeCallbacks(updateWallpaper);
        }

        private void updateWallpaper() {
            registerForSunPhaseChanges();
            startSunsetService();
        }

        private void startSunsetService() {
            final Intent service = new Intent(this.context, SunsetService.class);
            service.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, false);
            this.context.startService(service);
        }

        private void registerForSunPhaseChanges() {
            final SunsetBroadcastReceiver receiver = new SunsetBroadcastReceiver(this);
            final IntentFilter intentFilter = new IntentFilter(SunsetService.ACTION_UPDATE);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            this.context.getApplicationContext().registerReceiver(receiver, intentFilter);
        }

        private void draw(final UserVisibleData uvd) {
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) canvas.drawColor(getBackgroundColour(uvd));
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
            this.handler.removeCallbacks(updateWallpaper);
            if (this.visible) {
                this.handler.postDelayed(updateWallpaper, 1000 * 60 * 60);
            }
        }

        private int getBackgroundColour(final UserVisibleData uvd) {
            return ContextCompat.getColor(this.context, uvd.getCurrentPhase().getBackgroundColor());
        }

        private class SunsetBroadcastReceiver extends BroadcastReceiver {
            private final SunWallpaper wallpaper;

            public SunsetBroadcastReceiver(final SunWallpaper wallpaper) {
                this.wallpaper = wallpaper;
            }

            @Override
            public void onReceive(final Context context, final Intent intent) {
                final UserVisibleData uvd = new UserVisibleData(context, intent);
                this.wallpaper.draw(uvd);
            }
        }
    }
}
