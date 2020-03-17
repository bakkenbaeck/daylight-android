package com.bakkenbaeck.sol.widget

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.View
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bakkenbaeck.sol.BaseApplication

import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.util.UserVisibleData
import com.bakkenbaeck.sol.view.MainActivity
import com.bakkenbaeck.sol.view.custom.SunView

class DashboardWidget : AppWidgetProvider() {

    companion object {
        internal fun updateAppWidget(context: Context) {
            registerForSunPhaseChanges(context)
            BaseApplication.instance.refreshLocation(false)
        }

        private fun registerForSunPhaseChanges(context: Context) {
            val receiver = SunsetBroadcastReceiver()
            val intentFilter = IntentFilter(BaseApplication.ACTION_UPDATE)
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

            context.applicationContext.registerReceiver(receiver, intentFilter)
        }
    }

    override fun onUpdate(context: Context,
                          appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context)
        }
    }

    private class SunsetBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val uvd = UserVisibleData(context, intent)
            val views = RemoteViews(context.packageName, R.layout.dashboard_widget)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DashboardWidget::class.java)
            setBackground(views, uvd)
            setTodaysMessage(context, views, uvd)
            setSunView(context, views, uvd)
            setLocationPermissionMessage(context, views, uvd)
            makeWidgetClickable(context, views)
            appWidgetManager.updateAppWidget(appWidgetManager.getAppWidgetIds(componentName), views)
        }

        private fun setBackground(views: RemoteViews,
                                  uvd: UserVisibleData) {
            val color = uvd.currentPhase.backgroundColor
            views.setInt(R.id.widget_container, "setBackgroundResource", color)
        }

        private fun setLocationPermissionMessage(
                context: Context,
                views: RemoteViews,
                uvd: UserVisibleData) {
            val locationPermissionGranted = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val visibility = if (locationPermissionGranted) View.GONE else View.VISIBLE
            val secColor = uvd.currentPhase.secondaryColor
            views.setViewVisibility(R.id.location_message, visibility)
            views.setTextColor(R.id.location_message, ContextCompat.getColor(context, secColor))
        }

        private fun setSunView(
                context: Context,
                views: RemoteViews,
                uvd: UserVisibleData) {
            val viewWidth = context.resources.getDimensionPixelSize(R.dimen.widget_width)
            val viewHeight = context.resources.getDimensionPixelSize(R.dimen.widget_height)

            val sunView = createSunView(context, uvd, viewWidth, viewHeight)
            renderSunView(views, viewWidth, viewHeight, sunView)
        }

        private fun setTodaysMessage(
                context: Context,
                views: RemoteViews,
                uvd: UserVisibleData) {
            val secColor = uvd.currentPhase.secondaryColor
            views.setTextViewText(R.id.todays_message, uvd.todaysMessage)
            views.setTextColor(R.id.todays_message, ContextCompat.getColor(context, secColor))
        }

        private fun createSunView(context: Context,
                                  uvd: UserVisibleData,
                                  viewWidth: Int,
                                  viewHeight: Int): SunView {
            val primaryColor = ContextCompat.getColor(context, uvd.currentPhase.primaryColor)
            return SunView(context).apply {
                layoutParams = LinearLayout.LayoutParams(viewWidth, viewHeight)
                setColor(primaryColor)
                setStartLabel(uvd.sunriseText)
                setEndLabel(uvd.sunsetText)
                setColor(primaryColor)
                setPercentProgress(uvd.progress)
            }
        }

        private fun renderSunView(views: RemoteViews,
                                  viewWidth: Int,
                                  viewHeight: Int,
                                  sunView: SunView) {
            sunView.measure(viewWidth, viewHeight)
            sunView.layout(0, 0, viewWidth, viewHeight)
            sunView.isDrawingCacheEnabled = true
            val bitmap = sunView.drawingCache
            views.setImageViewBitmap(R.id.sun_view, bitmap)
        }

        private fun makeWidgetClickable(context: Context, views: RemoteViews) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        }
    }
}

