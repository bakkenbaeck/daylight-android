package com.bakkenbaeck.sol.util;

import android.content.Context;

import com.bakkenbaeck.sol.R;

public class MessageUtil {

    public static final String[] getPositiveDayMessages(final Context context){
        return context.getResources().getStringArray(R.array.daily_messages_positive);
    }

    public static final String[] getNegativeDayMessages(final Context context){
        return context.getResources().getStringArray(R.array.daily_messages_negative);
    }

    public static final String[] getPositiveNightMessages(final Context context){
        return context.getResources().getStringArray(R.array.night_messages_posivite);
    }

    public static final String[] getNegativeNightMessages(final Context context){
        return context.getResources().getStringArray(R.array.night_messages_negative);
    }

}
