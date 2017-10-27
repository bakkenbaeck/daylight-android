package com.bakkenbaeck.sol.widget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.util.UserVisibleData;
import com.bakkenbaeck.sunviewlib.SunView;

public class DashboardWidget extends AppWidgetProvider {

    static void updateAppWidget(final Context context,
                                final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {
        registerForSunPhaseChanges(context);
        startSunsetService(context);
    }

    private static void startSunsetService(final Context context) {
        final Intent service = new Intent(context, SunsetService.class);
        service.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, false);
        context.startService(service);
    }

    @Override
    public void onUpdate(final Context context,
                         final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void registerForSunPhaseChanges(final Context context) {
        final SunsetBroadcastReceiver receiver = new SunsetBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter(SunsetService.ACTION_UPDATE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        context.getApplicationContext().registerReceiver(receiver, intentFilter);
    }

    private static class SunsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final UserVisibleData uvd = new UserVisibleData(context, intent);
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dashboard_widget);
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            final ComponentName componentName = new ComponentName(context, DashboardWidget.class);
            setTodaysMessage(context, views, uvd);
            setSunView(context, views, uvd);
            setLocationPermissionMessage(context, views, uvd);
            appWidgetManager.updateAppWidget(appWidgetManager.getAppWidgetIds(componentName), views);
        }

        private void setLocationPermissionMessage(
                final Context context,
                final RemoteViews views,
                final UserVisibleData uvd) {
            final boolean locationPermissionGranted = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            final int visibility = locationPermissionGranted ? View.GONE : View.VISIBLE;
            final int secColor = uvd.getCurrentPhase().getSecondaryColor();
            views.setViewVisibility(R.id.location_message, visibility);
            views.setTextColor(R.id.location_message, ContextCompat.getColor(context, secColor));
        }

        private void setSunView(
                final Context context,
                final RemoteViews views,
                final UserVisibleData uvd) {
            final int viewWidth = context.getResources().getDimensionPixelSize(R.dimen.widget_width);
            final int viewHeight = context.getResources().getDimensionPixelSize(R.dimen.widget_height);

            final SunView sunView = createSunView(context, uvd, viewWidth, viewHeight);
            renderSunView(views, viewWidth, viewHeight, sunView);
        }

        private void setTodaysMessage(
                final Context context,
                final RemoteViews views,
                final UserVisibleData uvd) {
            final int secColor = uvd.getCurrentPhase().getSecondaryColor();
            views.setTextViewText(R.id.todays_message, uvd.getTodaysMessage());
            views.setTextColor(R.id.todays_message, ContextCompat.getColor(context, secColor));
        }

        private SunView createSunView(final Context context, final UserVisibleData uvd, final int viewWidth, final int viewHeight) {
            final int priColor = uvd.getCurrentPhase().getPrimaryColor();
            final SunView sunView = new SunView(context);
            sunView.setLayoutParams(new LinearLayout.LayoutParams(viewWidth, viewHeight));
            sunView.setColor(ContextCompat.getColor(context, priColor));
            sunView.setStartLabel(uvd.getSunriseText());
            sunView.setEndLabel(uvd.getSunsetText());
            sunView.setColor(ContextCompat.getColor(context, priColor))
                    .setStartLabel(uvd.getSunriseText())
                    .setEndLabel(uvd.getSunsetText())
                    .setPercentProgress(uvd.getProgress());
            return sunView;
        }

        private void renderSunView(final RemoteViews views, final int viewWidth, final int viewHeight, final SunView sunView) {
            sunView.measure(viewWidth, viewHeight);
            sunView.layout(0,0,viewWidth,viewHeight);
            sunView.setDrawingCacheEnabled(true);
            final Bitmap bitmap = sunView.getDrawingCache();
            views.setImageViewBitmap(R.id.sun_view, bitmap);
        }
    }
}

