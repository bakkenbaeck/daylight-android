package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bakkenbaeck.sol.R;
import org.joda.time.DateTime;

public class DailyMessage {

    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
    }

    public String get(final String city, final int numMinutes, final DayLengthDifference difference) {
        final String diffText = getDiffText(difference);

        String message = getRawMessage();
        message = message.replace("{city}", city);
        message = message.replace("{numMinutes}", String.valueOf(numMinutes));
        message = message.replace("{moreOrLess}", diffText);
        return message;
    }

    @NonNull
    private String getDiffText(final DayLengthDifference difference) {
        return difference == DayLengthDifference.LONGER
        ? context.getResources().getString(R.string.more)
        : context.getResources().getString(R.string.fewer);
    }

    private String getRawMessage() {
        final int dayOfYear = DateTime.now().dayOfYear().get();
        final String[] allMessages = this.context.getResources().getStringArray(R.array.daily_message);
        final int messagePosition = dayOfYear % allMessages.length;
        return allMessages[messagePosition];
    }
}
