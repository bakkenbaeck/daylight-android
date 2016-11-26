package com.bakkenbaeck.sol.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;

import com.bakkenbaeck.sol.R;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;

public class DailyMessage {

    private final Context context;

    public DailyMessage(final Context context) {
        this.context = context;
    }

    public Spanned get(final String city, final Period dayLengthChange) {
        final String diffText = getDiffText(dayLengthChange);
        final int numMinutes =
                Math.abs(dayLengthChange.getMinutes())
                + Math.abs(dayLengthChange.getSeconds()) / 30;

        String message = getRawMessage();
        message = message.replace("{city}", city);
        message = message.replace("{numMinutes}", String.valueOf(numMinutes));
        message = message.replace("{moreOrLess}", diffText);
        return convertToHtml(message);
    }

    private Spanned convertToHtml(final String message) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(message);
        }
    }

    @NonNull
    private String getDiffText(final Period dayLengthChange) {
        final Seconds dayLengthChangeInSeconds = dayLengthChange.toStandardSeconds();

        return
                dayLengthChangeInSeconds.isGreaterThan(Seconds.ZERO)
                        ? context.getResources().getString(R.string.more)
                        : context.getResources().getString(R.string.less);
    }

    private String getRawMessage() {
        final int dayOfYear = DateTime.now().dayOfYear().get();
        final String[] allMessages = this.context.getResources().getStringArray(R.array.daily_message);
        final int messagePosition = dayOfYear % allMessages.length;
        return allMessages[messagePosition];
    }
}
