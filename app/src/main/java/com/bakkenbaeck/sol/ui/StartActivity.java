package com.bakkenbaeck.sol.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.bakkenbaeck.sol.R;
import com.bakkenbaeck.sol.databinding.ActivityStartBinding;

public class StartActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForLocationPermission();
    }

    private void checkForLocationPermission() {
        final boolean locationPermissionGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!locationPermissionGranted) {
            initView();
        } else {
            Intent intent = new Intent(this, SunActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void initView() {
        assignClickListeners();
    }

    private void assignClickListeners() {
        ActivityStartBinding binding  = DataBindingUtil.setContentView(this, R.layout.activity_start);
        binding.infoMessage.setText(convertToHtml(getRequestLocationMessage()));
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(StartActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE_LOCATION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            final Intent intent = new Intent(this, SunActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private String getRequestLocationMessage() {
        final String message = getString(R.string.start__permission_description);
        final int color = ContextCompat.getColor(this, R.color.daylight_text2);
        return message.replace("{color}", String.valueOf(color));
    }

    private Spanned convertToHtml(final String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }
}
