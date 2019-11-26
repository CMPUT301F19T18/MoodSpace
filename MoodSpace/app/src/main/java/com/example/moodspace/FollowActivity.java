package com.example.moodspace;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FollowActivity extends AppCompatActivity
        implements ControllerCallback, FollowController.GetDataCallback {
    private static final String TAG = FollowActivity.class.getSimpleName();
    private FollowController fc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fc = new FollowController(this);

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
                case USERNAME_EXISTS:
                    return;
                case USERNAME_DOESNT_EXIST:
                    return;

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

