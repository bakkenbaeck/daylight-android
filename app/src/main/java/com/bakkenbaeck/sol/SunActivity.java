package com.bakkenbaeck.sol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sun);

        calculateOslo();
    }

    private void calculateOslo() {
        Location location = new Location("59.9139", "10.7522");
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Oslo");
        String officialSunrise = calculator.getOfficialSunriseForDate(Calendar.getInstance());
        String officialSunset = calculator.getOfficialSunsetForDate(Calendar.getInstance());

        final Date sunriseTime = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance()).getTime();
        final Date sunsetTime = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance()).getTime();

        long secs = (sunsetTime.getTime() - sunriseTime.getTime()) / 1000;
        int hours = (int) (secs / 3600);
        secs = secs % 3600;
        int mins = (int) (secs / 60);
        final String difference = hours + ":" + mins;


        ((TextView)findViewById(R.id.text_up)).setText(officialSunrise);
        ((TextView)findViewById(R.id.text_down)).setText(officialSunset);
        ((TextView)findViewById(R.id.text_total)).setText(difference);
    }
}
