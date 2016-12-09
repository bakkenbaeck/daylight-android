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
import com.bakkenbaeck.sol.util.SunPhaseUtil;
import com.florianmski.suncalc.models.SunPhase;

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

            String sunriseTime = intent.getStringExtra(SunsetService.EXTRA_SUNRISE_TIME);
            String sunsetTime = intent.getStringExtra(SunsetService.EXTRA_SUNSET_TIME);
            String locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE);

            double latitude = intent.getDoubleExtra(SunsetService.LOCATION_LATITUDE, 0);
            double longitude = intent.getDoubleExtra(SunsetService.LOCATION_LONGITUDE, 0);

            updateView(latitude, longitude, todaysMessageFormatted, sunriseTime, sunsetTime, locationMessage);
        }

        private Spanned convertToHtml(final String message) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
            } else {
                return Html.fromHtml(message);
            }
        }
    }

    private void updateView(final double latitude, final double longitude, Spanned todaysText,
                            String sunriseTime, String sunsetTime, String locationMessage) {
        SunPhase phase = SunPhaseUtil.getSunPhase(latitude, longitude);
        int color = SunPhaseUtil.getBackgroundColor(phase.getName().toString());
        int secColor = SunPhaseUtil.getSecColor(phase.getName().toString());
        int priColor = SunPhaseUtil.getPriColor(phase.getName().toString());

        binding.todaysMessage.setText(todaysText);
        binding.activitySun.setBackgroundColor(ContextCompat.getColor(this, color));
        binding.todaysMessage.setTextColor(ContextCompat.getColor(this, secColor));
        binding.location.setTextColor(ContextCompat.getColor(this, secColor));
        binding.share.setTextColor(ContextCompat.getColor(this, secColor));
        binding.location.setText(locationMessage);

        binding.sunView.setColor(ContextCompat.getColor(this, priColor));
        binding.sunView.setStartLabel(sunriseTime);
        binding.sunView.setEndLabel(sunsetTime);
        binding.sunView.setFloatingLabel(DateTime.now().toString("HH:mm"));
    }
}
