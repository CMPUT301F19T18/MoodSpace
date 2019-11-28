package com.example.moodspace;


import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;

import static com.example.moodspace.Utils.getSetFromUser;

/**
 * Communicates filtering options between the UI and the firestore database
 *
 * - Currently it stores filters as an array named by the const FILTERS_ARRAY
 *   - if the emotion is in the array, it will be filtered OUT.
 *   - this means that all users start out with no array
 */
public class FilterController implements ControllerCallback {
    private static final String TAG = FilterController.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String FILTERS_ARRAY = "filters";

    public static final String IS_SUCCESSFUL_KEY = "moodspace.FilterController.isSuccessfulKey";

    private ControllerCallback cc;
    private UserController uc;


    public FilterController(ControllerCallback cc) {
        this.cc = cc;
        this.uc = new UserController(this);
    }

    public interface GetFiltersCallback {
        void callbackFilters(@NonNull String user, @NonNull HashSet<String> filters);
    }

    /**
     * Gets the emotions that are to be filtered out
     */
    public void getFilters(final String username) {
        uc.getUserData(username, new UserController.CallbackUser() {
            @Override
            public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
                final HashSet<String> filtersSet = getSetFromUser(fetchedUserData, FILTERS_ARRAY);
                ((GetFiltersCallback) cc).callbackFilters(username, filtersSet);
            }
        });
    }

    public void updateFilters(String username, boolean[] initialChecks, boolean[] newChecks) {
        // counts how many are different first for the counter
        // assumes initialChecks.length == changedChecks.length

        int changedChecks = 0;
        for (int i = 0; i < initialChecks.length; i++) {
            if (initialChecks[i] != newChecks[i]) {
                changedChecks++;
            }
        }

        // nothing to update
        if (changedChecks == 0) {
            Log.d(TAG, "successfully updated filters (nothing changed)");
            return;
        }

        // otherwise, at least one check was changed
        CompletedTaskCounter counter = new CompletedTaskCounter(changedChecks);
        Emotion[] emotionArray = Emotion.values();
        for (int i = 0; i < initialChecks.length; i++) {
            if (initialChecks[i] != newChecks[i]) {
                if (newChecks[i]) {
                    // false to true means you filter the mood out
                    // (so you add the filter to firestore)
                    this.filterAdd(username, emotionArray[i], counter);
                } else {
                    // true to false means you undo the filter
                    // (so you remove the filter to firestore)
                    this.filterRemove(username, emotionArray[i], counter);
                }
            }
        }
    }

    /**
     * removes a filter to firestore
     */
    public void filterRemove(final String username, final Emotion emotion, final CompletedTaskCounter counter) {
        db.collection("users")
                .document(username)
                .update(FILTERS_ARRAY, FieldValue.arrayRemove(emotion))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("successfully removed %s from %s", emotion.getEmojiName(), username));
                        filterComplete(counter, username, true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to remove %s from %s", emotion.getEmojiName(), username));
                        cc.callback(FilterCallbackId.UPDATE_FILTER_FAIL);
                        filterComplete(counter, username, false);
                    }
                });
    }

    /**
     * adds a filter to firestore
     */
    public void filterAdd(final String username, final Emotion emotion, final CompletedTaskCounter counter) {
        db.collection("users")
                .document(username)
                .update(FILTERS_ARRAY, FieldValue.arrayRemove(emotion))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("successfully add %s from %s", emotion.getEmojiName(), username));
                        filterComplete(counter, username, true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to add %s from %s", emotion.getEmojiName(), username));
                        cc.callback(FilterCallbackId.UPDATE_FILTER_FAIL);
                        filterComplete(counter, username, false);
                    }
                });
    }


    /**
     * Callback only once all sub-tasks are complete as according to the counter
     */
    private void filterComplete(final CompletedTaskCounter counter, String username, boolean isSuccess) {
        counter.incrementComplete();
        if (isSuccess) {
            counter.incrementSuccess();
        }

        if (counter.isComplete()) {
            Log.d(TAG, "completed filter update with " + username);

            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_SUCCESSFUL_KEY, counter.isSuccess());

            cc.callback(FilterCallbackId.UPDATE_FILTERS_COMPLETE, bundle);
        }
    }


    @Override
    public void callback(CallbackId callbackId) {
        cc.callback(callbackId, null);
    }

    /**
     * Forwards all callbacks from FilterController to the normal ControllerCallback (activity)
     */
    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        cc.callback(callbackId, bundle);
    }
}
