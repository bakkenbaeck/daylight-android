package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivitySunBinding;
import com.bakkenbaeck.sol.service.AlarmReceiver;
import com.bakkenbaeck.sol.service.SunsetService;

public class SunActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private SunsetBroadcastReceiver sunsetBroadcastReceiver = new SunsetBroadcastReceiver();

    private ActivitySunBinding binding;
    private PendingIntent pendingIntent;
    private long tomorrowsSunrise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        checkForLocationPermission();
    }

    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            startLocationService();
        }
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_sun);
        this.sunsetBroadcastReceiver = new SunsetBroadcastReceiver();

        final Intent alarmIntent = new Intent(SunActivity.this, AlarmReceiver.class);
        this.pendingIntent = PendingIntent.getBroadcast(SunActivity.this, 0, alarmIntent, 0);

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        final Intent serviceIntent = new Intent(this, SunsetService.class);
        serviceIntent.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, true);
        startService(serviceIntent);

        final IntentFilter intentFilter = new IntentFilter(SunsetService.ACTION_UPDATE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(this.sunsetBroadcastReceiver, intentFilter);
    }

    private class SunsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String todaysMessage = intent.getStringExtra(SunsetService.EXTRA_DAILY_MESSAGE);
            final Spanned todaysMessageFormatted = convertToHtml(todaysMessage);
            binding.todaysMessage.setText(todaysMessageFormatted);

            final String todaysDate = intent.getStringExtra(SunsetService.EXTRA_TODAYS_DATE);
            binding.todaysDate.setText(todaysDate);

            tomorrowsSunrise = intent.getLongExtra(SunsetService.EXTRA_TOMORROWS_SUNRISE, 0);

            binding.loadingSpinner.setVisibility(View.GONE);

            enableAlarm();
        }

        private Spanned convertToHtml(final String message) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(message);
            }
        }
    }

    private void enableAlarm() {
        if (this.tomorrowsSunrise == 0) {
            return;
        }

        final AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP, this.tomorrowsSunrise, pendingIntent);
    }
}
