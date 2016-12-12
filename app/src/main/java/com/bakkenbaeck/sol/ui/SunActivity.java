package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivitySunBinding;
import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.util.CurrentPhase;
import com.bakkenbaeck.sol.util.SolPreferences;
import com.bakkenbaeck.sol.util.SunPhaseUtil;
import com.florianmski.suncalc.models.SunPhase;

import org.joda.time.DateTime;

import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class SunActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    private SunsetBroadcastReceiver sunsetBroadcastReceiver;
    private TimeReceiver timeReceiver;

    private ActivitySunBinding binding;
    private boolean firstTime = true;

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

        registerTimeReceiver();
    }

    private void registerTimeReceiver() {
        this.timeReceiver = new TimeReceiver();
        this.registerReceiver(this.timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            startLocationService();
        }
    }

    private void updateInfoViewIfAttached(final String phaseName) {
        InfoFragment f = (InfoFragment) getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG);

        if (f == null) {
            return;
        }

        f.update(phaseName);
    }

    private void showInfo(final String phaseName) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.container, InfoFragment.newInstance(phaseName), InfoFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(InfoFragment.TAG)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        final Intent serviceIntent = new Intent(this, SunsetService.class);
        SolPreferences solPrefs = new SolPreferences(this);
        serviceIntent.putExtra(SunsetService.EXTRA_SHOW_NOTIFICATION, solPrefs.getShowNotification());
        startService(serviceIntent);

        final IntentFilter intentFilter = new IntentFilter(SunsetService.ACTION_UPDATE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(this.sunsetBroadcastReceiver, intentFilter);
    }

    private class SunsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String todaysMessage = intent.getStringExtra( SunsetService.EXTRA_DAILY_MESSAGE);
            final Spanned todaysMessageFormatted = convertToHtml(todaysMessage);

            final String sunriseTime = intent.getStringExtra(SunsetService.EXTRA_SUNRISE_TIME);
            final String sunsetTime = intent.getStringExtra(SunsetService.EXTRA_SUNSET_TIME);
            final String locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE);
            final CurrentPhase currentPhase = new CurrentPhase(intent.getStringExtra(SunsetService.EXTRA_CURRENT_PHASE));

            double lat = intent.getDoubleExtra(SunsetService.EXTRA_LAT, 0);
            double lon = intent.getDoubleExtra(SunsetService.EXTRA_LON, 0);

            updateView(todaysMessageFormatted, sunriseTime, sunsetTime, locationMessage, currentPhase, lat, lon);
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
                            final CurrentPhase currentPhase,
                            final double lat,
                            final double lon) {

        if (this.binding == null) {
            return;
        }

        final int color = currentPhase.getBackgroundColor();
        final int secColor = currentPhase.getSecondaryColor();
        final int priColor = currentPhase.getPrimaryColor();

        if (this.firstTime) {
            this.binding.todaysMessage.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            this.binding.sunView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            this.binding.location.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            this.binding.share.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }

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
        this.binding.title.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.sunCircle.setColorFilter(ContextCompat.getColor(this, secColor), PorterDuff.Mode.SRC);
        this.getWindow().setBackgroundDrawableResource(color);

        if (!this.binding.titleWrapper.hasOnClickListeners()) {
            this.binding.titleWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInfo(currentPhase.getName());
                }
            });
        }

        updateInfoViewIfAttached(currentPhase.getName());

        // Calc current position in the sun view
        SunPhase sunrise = SunPhaseUtil.getSunPhase(lat, lon, "Sunrise");
        SunPhase sunset = SunPhaseUtil.getSunPhase(lat, lon, "Sunset");

        final Calendar startDate = sunrise.getStartDate();
        final Calendar endDate = sunset.getEndDate();
        final Calendar now = Calendar.getInstance();

        long span = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        long current = Calendar.getInstance().getTimeInMillis() - startDate.getTimeInMillis();

        double progress = (double) current / (double) span;
        double position = (Math.PI + (progress * Math.PI));
        double x = (50 + Math.cos(position) * 50) / 100;
        double y = (Math.abs(Math.sin(position) * 100)) / 100;

        if (progress > 1 || progress < 0) {
            x = 0;
            y = 0;
        }

        final Calendar startEndDate = sunrise.getEndDate();
        final Calendar endStartDate = sunset.getStartDate();

        boolean overHorizon = now.getTimeInMillis() >= startEndDate.getTimeInMillis() &&
                now.getTimeInMillis() <= endStartDate.getTimeInMillis();

        binding.sunView.setColor(ContextCompat.getColor(this, priColor))
                .setStartLabel(sunriseTime)
                .setEndLabel(sunsetTime)
                .setFloatingLabel(DateTime.now().toString("HH:mm"))
                .setEntireSunOverHorizon(overHorizon)
                .setProgress(x, y);

        firstTime = false;
    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() >= 1) {
            fm.popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sunsetBroadcastReceiver != null) {
            this.unregisterReceiver(sunsetBroadcastReceiver);
        }

        if (timeReceiver != null) {
            this.unregisterReceiver(timeReceiver);
        }

        if (binding != null) {
            binding.unbind();
            binding = null;
        }
    }
}

