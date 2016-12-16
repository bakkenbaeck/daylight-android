package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.util.Log;

import com.bakkenbaeck.sol.R;

public class MessageUtil {

    public static final String[] getPositiveDayMessages(final Context context) {
        Log.d("Sol", "getPositiveDayMessages: ");
        return context.getResources().getStringArray(R.array.daily_messages_positive);
    }

    public static final String[] getNegativeDayMessages(final Context context) {
        Log.d("Sol", "getNegativeDayMessages: ");
        return context.getResources().getStringArray(R.array.daily_messages_negative);
    }

    public static final String[] getPositiveNightMessages(final Context context) {
        Log.d("Sol", "getPositiveNightMessages: ");
        return context.getResources().getStringArray(R.array.night_messages_positive);
    }

    public static final String[] getNegativeNightMessages(final Context context) {
        Log.d("Sol", "getNegativeNightMessages: ");
        return context.getResources().getStringArray(R.array.night_messages_negative);
    }

    public static final String[] getNeutralMessages(final Context context) {
        return context.getResources().getStringArray(R.array.daily_messages_no_change);
    }
}
