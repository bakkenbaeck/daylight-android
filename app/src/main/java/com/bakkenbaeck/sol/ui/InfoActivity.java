package com.bakkenbaeck.sol.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivityInfoBinding;
import com.bakkenbaeck.sol.service.SunsetService;
import com.bakkenbaeck.sol.util.CurrentPhase;
import com.bakkenbaeck.sol.util.SolPreferences;

public class InfoActivity extends BaseActivity {
    public static final String PHASE_NAME = "phase_name";

    private SunsetBroadcastReceiver sunsetBroadcastReceiver;
    private ActivityInfoBinding binding;
    private boolean notificationEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_info);

        setColorsFromPhaseName();
        initNotificationsToggle();
        assignClickListeners();
        registerForSunPhaseChanges();
    }

    private void setColorsFromPhaseName() {
        final String phaseName = getIntent().getStringExtra(PHASE_NAME);

        final CurrentPhase currentPhase = new CurrentPhase(phaseName);
        final String infoMessage = getResources().getString(R.string.info_message);
        final String formatedInfo = infoMessage.replace("{color}", String.valueOf(ContextCompat
                .getColor(this, currentPhase.getPrimaryColor())));
        final Spanned s = convertToHtml(formatedInfo);
        this.binding.infoMessage.setText(s);

        final int color = currentPhase.getBackgroundColor();
        final int priColor = currentPhase.getPrimaryColor();
        final int secColor = currentPhase.getSecondaryColor();

        this.binding.title.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.infoMessage.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.notificationText.setTextColor(ContextCompat.getColor(this, secColor));
        this.binding.notificationValue.setTextColor(ContextCompat.getColor(this, priColor));

        final int colorFrom = ((ColorDrawable) this.binding.root.getBackground()).getColor();
        final int colorTo = ContextCompat.getColor(this, color);
        animateBackground(colorFrom, colorTo);

        setSunDrawable(currentPhase);
    }

    private void initNotificationsToggle() {
        final SolPreferences solPrefs = new SolPreferences(this);
        this.notificationEnabled = solPrefs.getShowNotification();

        final String s = notificationEnabled ? getString(R.string.off) : getString(R.string.on);
        this.binding.notificationValue.setText(s);
    }

    private void assignClickListeners() {
        this.binding.notificationWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationEnabled = !notificationEnabled;
                final String s = notificationEnabled ? getString(R.string.off) : getString(R.string.on);
                binding.notificationValue.setText(s);

                final SolPreferences solPrefs = new SolPreferences(InfoActivity.this);
                solPrefs.setShowNotification(notificationEnabled);
            }
        });

        this.binding.titleWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void registerForSunPhaseChanges() {
        final IntentFilter intentFilter = new IntentFilter(SunsetService.ACTION_UPDATE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        this.sunsetBroadcastReceiver = new SunsetBroadcastReceiver();
        registerReceiver(this.sunsetBroadcastReceiver, intentFilter);
    }

    private class SunsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            setColorsFromPhaseName();
        }
    }

    public void setSunDrawable(final CurrentPhase currentPhase) {
        final int color = currentPhase.getSecondaryColor();

        switch (color) {
            case R.color.sunrise_text: {
                this.binding.sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_sunrise));
                break;
            }
            case R.color.daylight_text: {
                this.binding.sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_daylight));
                break;
            }
            case R.color.sunset_text: {
                this.binding.sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_daylight));
                break;
            }
            case R.color.twilight_text: {
                this.binding.sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_twilight));
                break;
            }
            case R.color.night_text: {
                this.binding.sunCircle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.half_circle_night));
                break;
            }
        }
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
                binding.root.setBackgroundColor(color);
            }

        });
        colorAnimation.start();
    }

    private Spanned convertToHtml(final String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (sunsetBroadcastReceiver != null) {
            unregisterReceiver(sunsetBroadcastReceiver);
            sunsetBroadcastReceiver = null;
        }
    }
}
