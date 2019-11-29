package com.example.moodspace;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
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

    /**
     * General toasts for warn, info and success
     */

    public static void makeWarnToast(Context context, String message) {
        makeWarnToast(context, message, Toast.LENGTH_LONG);
    }
    public static void makeWarnToast(Context context, String message, int length) {
        // light red
        makeToast(context, message, length, Color.parseColor("#ff9999"));
    }

    public static void makeInfoToast(Context context, String message) {
        makeInfoToast(context, message, Toast.LENGTH_SHORT);
    }
    public static void makeInfoToast(Context context, String message, int length) {
        makeToast(context, message, length, null);
    }

    public static void makeSuccessToast(Context context, String message) {
        makeSuccessToast(context, message, Toast.LENGTH_SHORT);
    }
    public static void makeSuccessToast(Context context, String message, int length) {
        // light green
        makeToast(context, message, length, Color.parseColor("#99ff99"));
    }

    public static void makeToast(Context context, String message, int length, Integer color) {
        Toast toast = Toast.makeText(context, message, length);

        // sets text color
        // https://stackoverflow.com/a/9432923
        if (color != null) {
            TextView v = toast.getView().findViewById(android.R.id.message);
            v.setTextColor(color);
            //toast.getView().setBackgroundColor(color);
        }
        toast.show();
    }


    /**
     * Gets a bundle with a key -> user mapping
     */
    public static Bundle newUserBundle(String key, User user) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, user);
        return bundle;
    }

    /**
     * Gets a bundle with a key -> string mapping
     */
    public static Bundle newStringBundle(String key, String string) {
        Bundle bundle = new Bundle();
        bundle.putString(key, string);
        return bundle;
    }

    /**
     * checks whether a string bundle is null and if the key has data
     */
    public static String getStringFromBundle(Bundle bundle, String key, View snackBarView,
                                             String warnBundleNull, String warnContentNull) {
        if (bundle == null) {
            Snackbar.make(snackBarView, warnBundleNull, Snackbar.LENGTH_LONG).show();
            return null;
        }
        if (bundle.getString(key, null) == null) {
            Snackbar.make(snackBarView, warnContentNull, Snackbar.LENGTH_LONG).show();
            return null;
        }

        return bundle.getString(key);
    }

    /**
     * checks whether a serializable bundle is null and if the key has data
     */
    public static Serializable getSerializableFromBundle(Bundle bundle, String key, View snackBarView,
                                                         String warnBundleNull, String warnContentNull) {
        if (bundle == null) {
            Snackbar.make(snackBarView, warnBundleNull, Snackbar.LENGTH_LONG).show();
            return null;
        }
        if (!bundle.containsKey(key) || bundle.getSerializable(key) == null) {
            Snackbar.make(snackBarView, warnContentNull, Snackbar.LENGTH_LONG).show();
            return null;
        }

        return bundle.getSerializable(key);
    }


}
