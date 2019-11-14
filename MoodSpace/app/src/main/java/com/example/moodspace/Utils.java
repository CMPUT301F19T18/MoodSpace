package com.example.moodspace;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is used to format date and time to a desired form to use as display
 * in list view or in the add/edit mood form.
 */
public final class Utils {
    private static final SimpleDateFormat DATE_FORMATTER
            = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat TIME_FORMATTER
            = new SimpleDateFormat("HH:mm", Locale.US);

    private Utils() {
        // do nothing
    }

    public static String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static String formatTime(Date date) {
        return TIME_FORMATTER.format(date);
    }

    /**
     * Creates an alert dialog and then exits the app
     */
    public static void displayCriticalError(final Activity activity, String message) {
        String formattedMessage = message + "\n\nThe app will now close.";
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Critical Error")
                .setMessage(formattedMessage)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // closes the app
                        // https://stackoverflow.com/a/32571691
                        activity.moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
