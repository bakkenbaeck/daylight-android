package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivitySunBinding;
import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.util.CurrentPhase;

import org.joda.time.DateTime;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class SunActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private SunsetBroadcastReceiver sunsetBroadcastReceiver = new SunsetBroadcastReceiver();
    private ActivitySunBinding binding;

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
        this.binding.sunView.setTypeface(TypefaceUtils.load(getAssets(), "fonts/gtamericalight.ttf"));
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        final Intent serviceIntent = new Intent(this, SunsetService.class);
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

            final String sunriseTime = intent.getStringExtra(SunsetService.EXTRA_SUNRISE_TIME);
            final String sunsetTime = intent.getStringExtra(SunsetService.EXTRA_SUNSET_TIME);
            final String locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE);
            final CurrentPhase currentPhase = new CurrentPhase(intent.getStringExtra(SunsetService.EXTRA_CURRENT_PHASE));

            updateView(todaysMessageFormatted, sunriseTime, sunsetTime, locationMessage, currentPhase);
        }

        private Spanned convertToHtml(final String message) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(message);
            }
        }
    }

    private void updateView(final Spanned todaysText,
                            final String sunriseTime,
                            final String sunsetTime,
                            final String locationMessage,
                            final CurrentPhase currentPhase) {

        final int color = currentPhase.getBackgroundColor();
        final int secColor = currentPhase.getSecondaryColor();
        final int priColor = currentPhase.getPrimaryColor();

        this.binding.todaysMessage.setText(todaysText);
        this.binding.activitySun.setBackgroundColor(ContextCompat.getColor(this, color));
        this.binding.todaysMessage.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.location.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.share.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.location.setText(locationMessage);
        this.binding.sunView.setColor(ContextCompat.getColor(this, priColor));
        this.binding.sunView.setStartLabel(sunriseTime);
        this.binding.sunView.setEndLabel(sunsetTime);
        this.binding.sunView.setFloatingLabel(DateTime.now().toString("HH:mm"));
    }
}

