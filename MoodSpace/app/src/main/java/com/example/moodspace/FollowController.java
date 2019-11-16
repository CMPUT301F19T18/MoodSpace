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

import static com.example.moodspace.Utils.newStringBundle;

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

    public static final String TARGET_USERNAME_KEY = "moodspace.FollowController.targetUsernameKey";
    public static final String IS_SUCCESSFUL_KEY = "moodspace.FollowController.isSuccessfulKey";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;
    private UserController uc;

    public FollowController(ControllerCallback cc) {
        this.cc = cc;
        this.uc = new UserController(this);
    }

    public interface Callback {
        void callbackFollowingMoods(@NonNull List<MoodOther> followingMoodsList);
        void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                @NonNull List<String> followers,
                                @NonNull List<String> followRequestsFrom,
                                @NonNull List<String> followRequestsTo);
    }


    /**
     * Used to generalize array additions/removals.
     *
     * @param user main user that is logged in
     * @param target any user that isn't the logged in user
     * @param arrayName follow-related array as defined in the constants of this class
     * @param getUserArray if true, gets user.array, otherwise gets target.array
     * @param isAddition whether it adds or removes from the array
     * @param logSuccess the log message if the task succeeds
     * @param logFail the log message if the task fails
     * @param failCallbackId the callbackId for when the modifier has failed
     * @param completeCallbackId the callbackId for when the modifier is complete
     */
    private void arrayModifier(final String user, final String target, final String arrayName,
                               final boolean getUserArray, boolean isAddition,
                               final String logSuccess, final String logFail,
                               final FollowCallbackId failCallbackId,
                               final FollowCallbackId completeCallbackId) {
        final HashSet<String> usersComplete = new HashSet<>();
        final HashSet<String> usersSuccessful = new HashSet<>();

        // access users
        String user1, user2;
        if (getUserArray) {
            user1 = user;
            user2 = target;
        } else {
            user1 = target;
            user2 = user;
        }

        // whether it adds or removes
        FieldValue modifier;
        if (isAddition) {
            modifier = FieldValue.arrayUnion(user2);
        } else {
            modifier = FieldValue.arrayRemove(user2);
        }

        db.collection("users")
                .document(user1)
                .update(arrayName, modifier)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, logSuccess);
                        callbackComplete(usersComplete, usersSuccessful, user, target,
                                true, completeCallbackId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, logFail);
                        cc.callback(failCallbackId);
                        usersComplete.add(user);
                        callbackComplete(usersComplete, usersSuccessful, user, target,
                                true, completeCallbackId);
                    }
                });
    }

    /**
     * Callback only once both tasks are complete, so when the size of usersComplete is 2.
     */
    private void callbackComplete(final HashSet<String> usersComplete, final HashSet<String> usersSuccessful,
                                  String user, String target, boolean isSuccess, FollowCallbackId completeCode) {
        usersComplete.add(user);
        if (isSuccess) {
            usersSuccessful.add(user);
        }

        if (usersComplete.size() == 2) {
            Log.d(TAG, String.format("%s with user=%s and target=%s", completeCode, user, target));
            Bundle bundle = new Bundle();
            bundle.putString(TARGET_USERNAME_KEY, target);
            bundle.putBoolean(IS_SUCCESSFUL_KEY, usersSuccessful.size() == 2);
            cc.callback(completeCode, bundle);
        }
    }

    /**
     * user => target
     */
    public void addFollower(final String user, final String target) {
        // user.following U {target}
        this.arrayModifier(user, target, FOLLOWING_ARRAY, true, true,
                String.format("successfully set %s to follow %s", user, target),
                String.format("failed to set %s to follow %s", user, target),
                FollowCallbackId.ADD_USER_TO_FOLLOWING_FAIL,
                FollowCallbackId.ADD_FOLLOWER_COMPLETE);

        // target.followers U {user}
        this.arrayModifier(user, target, FOLLOWERS_ARRAY, false, true,
                String.format("successfully set %s to have %s as a follower", target, user),
                String.format("failed to set %s to have %s as a follower", target, user),
                FollowCallbackId.ADD_USER_AS_FOLLOWER_FAIL,
                FollowCallbackId.ADD_FOLLOWER_COMPLETE);
    }

    /**
     * user =/=> target
     * (unfollow)
     */
    public void removeFollower(final String user, final String target) {
        // user.following \ {target}
        this.arrayModifier(user, target, FOLLOWING_ARRAY, true, false,
                String.format("successfully removed %s from following %s", user, target),
                String.format("failed to remove %s from following %s", user, target),
                FollowCallbackId.REMOVE_FROM_FOLLOWING_FAIL,
                FollowCallbackId.REMOVE_FOLLOWER_COMPLETE);

        // target.followers \ {user}
        this.arrayModifier(user, target, FOLLOWERS_ARRAY, false, false,
                String.format("successfully removed %s from being a follower of %s", target, user),
                String.format("failed to remove %s from being a follower of %s", target, user),
                FollowCallbackId.REMOVE_AS_FOLLOWER_FAIL,
                FollowCallbackId.REMOVE_FOLLOWER_COMPLETE);
    }

    /**
     * user -> target
     */
    public void sendFollowRequest(final String user, final String target) {
        // user.follow_requests_to U {target}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_TO_ARRAY, true, true,
                String.format("successfully uploaded pending follow request (%s -> %s)", user, target),
                String.format("failed to upload pending follow request (%s -> %s)", user, target),
                FollowCallbackId.ADD_FOLLOW_REQUEST_TO_FAIL,
                FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE);

        // target.follow_requests_from U {user}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_FROM_ARRAY, false, true,
                String.format("successfully set %s to have a follow request from %s", target, user),
                String.format("failed to set %s to have a follow request from %s", target, user),
                FollowCallbackId.ADD_FOLLOW_REQUEST_FROM_FAIL,
                FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE);
    }

    /**
     * user -/-> potentialFollowee
     * (also used for declining follow requests)
     */
    public void removeFollowRequest(final String user, final String target) {
        // user.follow_requests_to \ {target}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_TO_ARRAY, true, false,
                String.format("successfully removed %s's follow req from %s's follow req to array", target, user),
                String.format("failed to remove %s's follow req from %s's follow req to array", target, user),
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_TO_FAIL,
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_COMPLETE);

        // target.follow_requests_from \ {user}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_FROM_ARRAY, false, false,
                String.format("successfully removed %s's follow req from %s's follow req from array", user, target),
                String.format("failed to remove %s's follow req from %s's follow req from array", user, target),
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_FROM_FAIL,
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_COMPLETE);
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
                                        cc.callback(FollowCallbackId.FOLLOWEE_MOOD_READ_FAIL);
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
        uc.getUserData(user, new UserController.CallbackUser() {
            @Override
            public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
                List<String> following = (List<String>) fetchedUserData.get(FOLLOWING_ARRAY);
                if (following == null) {
                    following = new ArrayList<>();
                }

                List<String> followers = (List<String>) fetchedUserData.get(FOLLOWERS_ARRAY);
                if (followers == null) {
                    followers = new ArrayList<>();
                }

                List<String> followRequestsFrom = (List<String>) fetchedUserData.get(FOLLOW_REQUESTS_FROM_ARRAY);
                if (followRequestsFrom == null) {
                    followRequestsFrom = new ArrayList<>();
                }

                List<String> followRequestsTo = (List<String>) fetchedUserData.get(FOLLOW_REQUESTS_TO_ARRAY);
                if (followRequestsTo == null) {
                    followRequestsTo = new ArrayList<>();
                }

                ((Callback) cc).callbackFollowData(user, following, followers, followRequestsFrom, followRequestsTo);

            }
        });
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
