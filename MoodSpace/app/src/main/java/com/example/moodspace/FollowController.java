package com.example.moodspace;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Followers can be thought of as a directed graph between users.
 * Both requests and following are represented in firestore as adjacency lists under:
 *   users -> (username) -> Following
 *   users -> (username) -> Followers
 *   users -> (username) -> FollowRequestsFrom
 *
 * For notation,let x and y be users.
 *   x => y means the following:
 *    - x is following y
 *    - x is a follower of y
 *    - y is a followee of x
 *    - "y" is contained in x's "Following" array
 *
 *   x -> y means the following:
 *    - x has sent a follow request to y
 *    - x wants to follow y
 *    - "x" is contained in y's "FollowRequestsFrom" array
 *
 * Note: A followee accepting a follow request does two things:
 *   - follower -/-> followee
 *   - follower => followee
 *
 * Note: The pairs "Following" & "Followers" and "FollowRequestsTo" & "FollowRequestsFrom" exist
 *   to reduce query time at the cost of having to maintaining both lists at the same time.
 */
public class FollowController implements ControllerCallback {
    private static final String TAG = FollowController.class.getSimpleName();
    private static final String FOLLOWING_ARRAY = "Following";
    private static final String FOLLOWERS_ARRAY = "Followers";
    private static final String FOLLOW_REQUESTS_FROM_ARRAY = "FollowRequestsFrom";
    private static final String FOLLOW_REQUESTS_TO_ARRAY = "FollowRequestsTo";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;
    private UserController uc;

    public FollowController(ControllerCallback cc) {
        this.cc = cc;
        this.uc = new UserController(this);
    }

    public interface Callback {
        void callbackFollowingMoods(@NonNull List<MoodOther> followingMoodsList);
        void callbackFollowData(@NonNull List<String> following, @NonNull List<String> followers,
                                @NonNull List<String> followRequestsFrom,
                                @NonNull List<String> followRequestsTo);
    }

    /**
     * Callback only once both tasks are complete, so when the size of usersComplete is 2.
     */
    private void callbackComplete(final HashSet<String> usersComplete, final HashSet<String> usersSuccessful,
                                  String user, boolean isSuccess,
                                  FollowCallbackId successCode, FollowCallbackId completeCode) {
        usersComplete.add(user);
        if (isSuccess) {
            usersSuccessful.add(user);
        }

        if (usersComplete.size() == 2) {
            cc.callback(completeCode);
            if (usersSuccessful.size() == 2) {
                cc.callback(successCode);
            }
        }


    }

    /**
     * user => target
     * TODO update both arrays
     */
    public void addFollower(final String user, final String target) {
        final HashSet<String> usersComplete = new HashSet<>();
        final HashSet<String> usersSuccessful = new HashSet<>();

        // set user to follow target
        db.collection("users")
                .document(user)
                .update(FOLLOWING_ARRAY, FieldValue.arrayUnion(target))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("successfully set %s to follow %s", user, target));
                        callbackComplete(usersComplete, usersSuccessful, user, true,
                                FollowCallbackId.ADD_FOLLOWER_SUCCESS, FollowCallbackId.ADD_FOLLOWER_COMPLETE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to set %s to follow %s", user, target));
                        cc.callback(FollowCallbackId.ADD_USER_FOLLOWING_FAIL);
                        usersComplete.add(user);
                        callbackComplete(usersComplete, usersSuccessful, user, false,
                                FollowCallbackId.ADD_FOLLOWER_SUCCESS, FollowCallbackId.ADD_FOLLOWER_COMPLETE);
                    }
                });

        // set target to have user following
        db.collection("users")
                .document(target)
                .update(FOLLOWERS_ARRAY, FieldValue.arrayUnion(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("successfully set %s to have %s as a follower", target, user));
                        callbackComplete(usersComplete, usersSuccessful, user, true,
                                FollowCallbackId.ADD_FOLLOWER_SUCCESS, FollowCallbackId.ADD_FOLLOWER_COMPLETE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to set %s to have %s as a follower", target, user));
                        cc.callback(FollowCallbackId.ADD_USER_AS_FOLLOWER_FAIL);
                        callbackComplete(usersComplete, usersSuccessful, user, false,
                                FollowCallbackId.ADD_FOLLOWER_SUCCESS, FollowCallbackId.ADD_FOLLOWER_COMPLETE);
                    }
                });

    }

    /**
     * user =/=> target
     * (unfollow)
     * TODO update both arrays
     */
    public void removeFollower(final String user, final String target) {
        final DocumentReference doc =

        final HashSet<String> usersComplete = new HashSet<>();
        final HashSet<String> usersSuccessful = new HashSet<>();

        db.collection("users")
                .document(user)
                .update(FOLLOWING_ARRAY, FieldValue.arrayRemove(target))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("remove follower success (%s => %s)", user, target));
                        cc.callback(REMOVE_FOLLOWER_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to remove follower (%s => %s)", user, target));
                        cc.callback(REMOVE_FOLLOWER_FAIL);
                    }
                });
    }

    /**
     * user -> target
     * TODO update both arrays
     */
    public void sendFollowRequest(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(target);

        doc.update(FOLLOW_REQUESTS_FROM_ARRAY, FieldValue.arrayUnion(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("add follow request success (%s -> %s)",
                                user, target));
                        cc.callback(SEND_REQUEST_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to add follow request (%s -> %s)",
                                user, target));
                        cc.callback(SEND_REQUEST_FAIL);
                    }
                });
    }

    /**
     * user -/-> potentialFollowee
     * (also used for declining follow requests)
     * TODO update both arrays
     */
    public void removeFollowRequest(final String user, final String target) {
        DocumentReference doc = db.collection("users").document(target);

        doc.update(FOLLOW_REQUESTS_FROM_ARRAY, FieldValue.arrayRemove(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("remove follow request success (%s -> %s)",
                                user, target));
                        cc.callback(REMOVE_REQUEST_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to remove follow request (%s => %s)",
                                user, target));
                        cc.callback(REMOVE_REQUEST_FAIL);
                    }
                });
    }

    /**
     * - gets all of the users that user is following & and for each followee, gets the most recent mood
     */
    public void getFollowingMoods(String user) {
        final HashSet<String> followingComplete = new HashSet<>();
        final List<MoodOther> followingMoods = new ArrayList<>();

        uc.getUserData(user, new UserController.CallbackUser() {
            @Override
            public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
                final List<String> followingList = (List<String>) fetchedUserData.get(FOLLOWING_ARRAY);
                if (followingList == null) {
                    return;
                }
                for (final String followee : followingList) {
                    db.collection("users")
                            .document(followee)
                            .collection("Moods")
                            .orderBy("date", Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "Error reading followee mood data for user " + followee);
                                        Log.d(TAG, Log.getStackTraceString(task.getException()));
                                        cc.callback(FOLLOWEE_MOOD_READ_FAIL);
                                        return;
                                    }
                                    // no mood (can happen if user's follower has no mood)
                                    if (task.getResult() != null ) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            Mood mood = (Mood) doc.getData();
                                            MoodOther moodOther = MoodOther.fromMood(mood, followee);
                                            followingMoods.add(moodOther);
                                        }
                                    }
                                    followingComplete.add(followee);

                                    // constantly checks whenever followingComplete is full
                                    if (followingComplete.size() == followingList.size()) {
                                        ((Callback) cc).callbackFollowingMoods(followingMoods);
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * Gets all following data (following, followers, follow requests to/from) for the given user
     */
    public void getFollowData(final String user) {
        // TODO stub
        //((Callback) cc).callbackFollowData();
    }

    @Override
    public void callback(CallbackId callbackId) {
        cc.callback(callbackId, null);
    }

    /**
     * Forwards all callbacks from UserController to the normal ControllerCallback (activity)
     */
    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        cc.callback(callbackId, bundle);
    }
}
