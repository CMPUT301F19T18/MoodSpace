package com.example.moodspace;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FollowActivity extends AppCompatActivity implements ControllerCallback, FollowController.Callback {
    private UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uc = new UserController(this);

    }

    @Override
    public void callback(String callbackId) {
        // TODO stub
        //View snackBarView = findViewById(R.id.whatever);
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

            case FollowController.FOLLOWEE_MOOD_READ_FAIL:
                return;

            case UserController.LOGIN_READ_FAIL:
                return;
            case UserController.USER_TASK_NULL:
                return;
            case UserController.USER_NONEXISTENT:
                return;
        }
    }

    @Override
    public void callbackFollowingMoods(@NonNull List<MoodOther> followingMoodsList) {

    }

    @Override
    public void callbackFollowData(@NonNull List<String> following, @NonNull List<String> followers,
                                   @NonNull List<String> followRequestsFrom,
                                   @NonNull List<String> followRequestsTo) {

    }
}

