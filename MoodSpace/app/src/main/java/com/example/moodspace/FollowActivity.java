package com.example.moodspace;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FollowActivity extends AppCompatActivity implements ControllerCallback {
    UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uc = new UserController(this);

    }

    @Override
    public void callback(String callbackId) {
        // TODO stub
    }
}
