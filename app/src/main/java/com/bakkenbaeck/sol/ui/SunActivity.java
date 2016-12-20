package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivitySunBinding;
import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.service.TimeReceiver;
import com.bakkenbaeck.sol.util.CurrentPhase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_sun);
        this.sunsetBroadcastReceiver = new SunsetBroadcastReceiver();
        this.binding.sunView.setTypeface(TypefaceUtils.load(getAssets(), "fonts/gtamericalight.ttf"));

        registerTimeReceiver();
    }

    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            startLocationService();
        }
    }

    private void registerTimeReceiver() {
        this.timeReceiver = new TimeReceiver();
        this.registerReceiver(this.timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        startLocationService();
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

            final long sunriseTime = intent.getLongExtra(SunsetService.EXTRA_SUNRISE_TIME, 0);
            final long sunsetTime = intent.getLongExtra(SunsetService.EXTRA_SUNSET_TIME, 0);
            final String locationMessage = intent.getStringExtra(SunsetService.EXTRA_LOCATION_MESSAGE);
            final CurrentPhase currentPhase = new CurrentPhase(intent.getStringExtra(SunsetService.EXTRA_CURRENT_PHASE));

            final SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault());
            final String sunriseText = sdf.format(sunriseTime);
            final String sunsetText = sdf.format(sunsetTime);

            updateView(todaysMessageFormatted, sunriseText, sunsetText, locationMessage,
                    currentPhase, sunriseTime, sunsetTime);
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
                            final long sunriseStart,
                            final long sunsetEnd) {

        if (this.binding == null) {
            return;
        }

        final int color = currentPhase.getBackgroundColor();
        final int secColor = currentPhase.getSecondaryColor();
        final int priColor = currentPhase.getPrimaryColor();

        final SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.hh_mm), Locale.getDefault());

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
        this.binding.todaysMessage.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.location.setTextColor(ContextCompat.getColor(this, secColor));

        this.binding.share.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.location.setText(locationMessage);

        this.binding.sunView.setColor(ContextCompat.getColor(this, priColor));
        this.binding.sunView.setStartLabel(sunriseTime);
        this.binding.sunView.setEndLabel(sunsetTime);
        this.binding.title.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.sunCircle.setColorFilter(ContextCompat.getColor(this, secColor), PorterDuff.Mode.SRC);
        this.binding.sunView.setFloatingLabel(sdf.format(new Date()));

        final int colorFrom = ((ColorDrawable) this.binding.activitySun.getBackground()).getColor();
        final int colorTo = ContextCompat.getColor(this, color);
        animateBackground(colorFrom, colorTo);

        this.binding.titleWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoActivity(currentPhase.getName());
            }
        });

        final long span = sunsetEnd - sunriseStart;
        final long current = Calendar.getInstance().getTimeInMillis() - sunriseStart;
        final double progress = (double) current / (double) span;

        binding.sunView.setColor(ContextCompat.getColor(this, priColor))
                .setStartLabel(sunriseTime)
                .setEndLabel(sunsetTime)
                .setFloatingLabel(sdf.format(new Date()))
                .setPercentProgress(progress);

        firstTime = false;
    }

    private void showInfoActivity(final String phaseName) {
        final Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra(InfoActivity.PHASE_NAME, phaseName);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void animateBackground(final int colorFrom, final int colorTo) {
        if (colorFrom == colorTo) {
            return;
        }

        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(400);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                if (binding == null) {
                    return;
                }
                int color = (int) animator.getAnimatedValue();
                binding.activitySun.setBackgroundColor(color);
            }

        });
        colorAnimation.start();
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
    }
}

