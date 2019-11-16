package com.example.moodspace;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FollowActivity extends AppCompatActivity implements ControllerCallback, FollowController.Callback {
    private static final String TAG = FollowActivity.class.getSimpleName();
    private UserController uc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uc = new UserController(this);

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
                default:
                    return;
            }
        } else if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case LOGIN_READ_FAIL:
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
    public void callbackFollowingMoods(@NonNull List<MoodOther> followingMoodsList) {
        // TODO stub
    }

    @Override
    public void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                   @NonNull List<String> followers,
                                   @NonNull List<String> followRequestsFrom,
                                   @NonNull List<String> followRequestsTo) {
        // TODO stub
    }
}

