package com.example.moodspace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import io.paperdb.Paper;

public class FollowActivity extends AppCompatActivity
        implements ControllerCallback, FollowController.GetDataCallback {
    private static final String TAG = FollowActivity.class.getSimpleName();
    private FollowController fc;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fc = new FollowController(this);
        setContentView(R.layout.activity_follow);
        username = getIntent().getExtras().getString("username");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // sets up the menu button
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // sets up navigation viewer (side bar)
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final TextView headerTextView
                = navigationView.getHeaderView(0).findViewById(R.id.header_text_view);
        headerTextView.setText(username);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_item_profile:
                        Intent intent = new Intent(FollowActivity.this, ProfileListActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        return true;
                    case R.id.nav_item_following:
                        Toast.makeText(FollowActivity.this,
                                "Following", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_map:
                        Intent intent1 = new Intent(FollowActivity.this, MapsActivity.class);
                        intent1.putExtra("username", username);
                        startActivity(intent1);
                        return true;
                    case R.id.nav_item_log_out:
                        Paper.book().delete(UserController.PAPER_USERNAME_KEY);
                        Paper.book().delete(UserController.PAPER_PASSWORD_KEY);
                        Intent loginScreen = new Intent(FollowActivity.this, LoginActivity.class);
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

    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        // TODO stub
        //View snackBarView = findViewById(R.id.whatever);
        if (callbackId instanceof FollowCallbackId) {
            switch ((FollowCallbackId) callbackId) {

                case ADD_FOLLOWER_COMPLETE:
                    return;
                case ADD_USER_TO_FOLLOWING_FAIL:
                    return;
                case ADD_USER_AS_FOLLOWER_FAIL:
                    return;

                case REMOVE_FOLLOWER_COMPLETE:
                    return;
                case REMOVE_FROM_FOLLOWING_FAIL:
                    return;
                case REMOVE_AS_FOLLOWER_FAIL:
                    return;

                case ADD_FOLLOW_REQUEST_COMPLETE:
                    return;
                case ADD_FOLLOW_REQUEST_TO_FAIL:
                    return;
                case ADD_FOLLOW_REQUEST_FROM_FAIL:
                    return;

                case REMOVE_FOLLOW_REQUEST_COMPLETE:
                    return;
                case REMOVE_FOLLOW_REQUEST_TO_FAIL:
                    return;
                case REMOVE_FOLLOW_REQUEST_FROM_FAIL:
                    return;

                case ACCEPT_FOLLOW_REQUEST_COMPLETE:
                    return;
                case FOLLOWEE_MOOD_READ_FAIL:
                    return;
                default:
                    return;
            }
        } else if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case USER_READ_DATA_FAIL:
                    return;
                case USER_TASK_NULL:
                    return;
                case USER_NONEXISTENT:
                    return;
                default:
                    return;
            }
        } else {
            Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
    }

    @Override
    public void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                   @NonNull List<String> followers,
                                   @NonNull List<String> followRequestsFrom,
                                   @NonNull List<String> followRequestsTo) {
        // TODO stub
    }
}

