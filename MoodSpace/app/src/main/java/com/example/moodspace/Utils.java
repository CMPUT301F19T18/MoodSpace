package com.example.moodspace;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;

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

    public static void setupNavbar(final Activity activity, Toolbar toolbar, String username) {
        // sets up the menu button
        final DrawerLayout drawerLayout = activity.findViewById(R.id.profile_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // sets up navigation viewer (side bar)
        final NavigationView navigationView = activity.findViewById(R.id.nav_view);
        final TextView headerTextView
                = navigationView.getHeaderView(0).findViewById(R.id.header_text_view);
        headerTextView.setText(username);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_item_profile:
                        Toast.makeText(activity.getClass(),
                                "Profile", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_following:
                        Intent intent = new Intent(ProfileListActivity.this, FollowActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        return true;
                    case R.id.nav_item_map:
                        Intent intent1 = new Intent(ProfileListActivity.this, MapsActivity.class);
                        intent1.putExtra("username", username);
                        startActivity(intent1);
                        return true;
                    case R.id.nav_item_log_out:
                        Paper.book().delete(UserController.PAPER_USERNAME_KEY);
                        Paper.book().delete(UserController.PAPER_PASSWORD_KEY);
                        Intent loginScreen = new Intent(ProfileListActivity.this, LoginActivity.class);
                        loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        finish();
                        startActivity(loginScreen);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }


    /**
     * Simply gets a bundle with a key -> user mapping
     */
    public static Bundle newUserBundle(String key, User user) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(key, user);
        return bundle;
    }

    /**
     * Simply gets a bundle with a key -> string mapping
     */
    public static Bundle newStringBundle(String key, String string) {
        Bundle bundle = new Bundle();
        bundle.putString(key, string);
        return bundle;
    }

}
