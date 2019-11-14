package com.example.moodspace;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FollowActivity extends AppCompatActivity implements ControllerCallback, FollowController.Callback {
    UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uc = new UserController(this);

    }

    @Override
    public void callback(String callbackId) {
        // TODO stub
        //View snackBarView = findViewById(R.id.login_view);
        switch (callbackId) {
            case FollowController.ADD_FOLLOWER_SUCCESS:
                return;
            case FollowController.ADD_FOLLOWER_FAIL:
                return;
            case FollowController.REMOVE_FOLLOWER_SUCCESS:
                return;
            case FollowController.REMOVE_FOLLOWER_FAIL:
                return;
            case FollowController.SEND_REQUEST_SUCCESS:
                return;
            case FollowController.SEND_REQUEST_FAIL:
                return;
            case FollowController.REMOVE_REQUEST_SUCCESS:
                return;
            case FollowController.REMOVE_REQUEST_FAIL:
                return;
        }
    }

    @Override
    public void callbackFollowingMoods(List<MoodOther> followingMoodsList) {
        // TODO stub
    }
}

