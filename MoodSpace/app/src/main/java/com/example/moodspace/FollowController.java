package com.example.moodspace;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.moodspace.Utils.getListFromUser;

/**
 * Followers can be thought of as a directed graph between users.
 * Both requests and following are represented in firestore as adjacency lists under:
 *   users -> (username) -> Following
 *   users -> (username) -> Followers
 *   users -> (username) -> FollowRequestsFrom
 *   users -> (username) -> FollowRequestsTo
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
 * Note: The pairs "Following" & "Followers" and "FollowRequestsTo" & "FollowRequestsFrom" exist
 *   to reduce query time at the cost of having to maintaining both lists at the same time.
 *
 * Note: Currently, removing a follower / follow req will return successful
 *   EVEN IF there was no follower / follow req avaliable to remove.
 */
public class FollowController implements ControllerCallback {
    private static final String TAG = FollowController.class.getSimpleName();
    private static final String FOLLOWING_ARRAY = "Following";
    private static final String FOLLOWERS_ARRAY = "Followers";
    private static final String FOLLOW_REQUESTS_FROM_ARRAY = "FollowRequestsFrom";
    private static final String FOLLOW_REQUESTS_TO_ARRAY = "FollowRequestsTo";

    public static final String IS_SUCCESSFUL_KEY = "moodspace.FollowController.isSuccessfulKey";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;
    private UserController uc;

    public FollowController(ControllerCallback cc) {
        this.cc = cc;
        this.uc = new UserController(this);
    }

    public interface OtherMoodsCallback {
        void callbackFollowingMoods(@NonNull String user, @NonNull ArrayList<MoodView> followingMoodsList);
    }

    public interface GetDataCallback {
        void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                @NonNull List<String> followers,
                                @NonNull List<String> followRequestsFrom,
                                @NonNull List<String> followRequestsTo);
    }


    /**
     * Used to generalize array additions/removals.
     * Note that target and user must be different users.
     *
     * @param user main user (currently logged in user)
     * @param target any user that isn't the main user
     * @param arrayName follow-related array as defined in the constants of this class
     * @param getUserArray if true, modifies target w.r.t. user.array,
     *                     otherwise modifies user w.r.t. target.array
     * @param isAddition whether it adds or removes from the array
     * @param logSuccess the log message if the task succeeds
     * @param logFail the log message if the task fails
     * @param counter stores the number of sub-tasks that must be completed until its parent task is complete
     * @param failCallbackId the callbackId for when the modifier has failed
     * @param completeCallbackId the callbackId for when the modifier is complete
     */
    private void arrayModifier(final String user, final String target, final String arrayName,
                               boolean getUserArray, boolean isAddition,
                               final String logSuccess, final String logFail,
                               final CompletedTaskCounter counter,
                               final FollowCallbackId failCallbackId,
                               final FollowCallbackId completeCallbackId) {
        Log.d(TAG, String.format("attempting %s for user=%s and target=%s",
                completeCallbackId.toString(), user, target));

        // if the user and the target are the same, simply returns unsuccessful
        if (user.equals(target)) {
            callbackComplete(counter, user, target, false, completeCallbackId);
            return;
        }

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
                        callbackComplete(counter, user, target, true, completeCallbackId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, logFail);
                        cc.callback(failCallbackId);
                        callbackComplete(counter, user, target, false, completeCallbackId);
                    }
                });
    }

    /**
     * Callback only once all sub-tasks are complete as according to the counter
     */
    private void callbackComplete(final CompletedTaskCounter counter, String user, String target, boolean isSuccess,
                                  FollowCallbackId completeCode) {
        counter.incrementComplete();
        if (isSuccess) {
            counter.incrementSuccess();
        }

        if (counter.isComplete()) {
            Log.d(TAG, String.format("completed %s with user=%s and target=%s", completeCode, user, target));

            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_SUCCESSFUL_KEY, counter.isSuccess());

            cc.callback(completeCode, bundle);
        }
    }

    /**
     * user => target
     */
    public void addFollower(final String user, final String target) {
        addFollower(user, target, null, null);
    }
    private void addFollower(String user, String target, CompletedTaskCounter counter,
                             FollowCallbackId completeCallbackId) {
        if (counter == null) {
            counter = CompletedTaskCounter.getDefault();
        }
        if (completeCallbackId == null) {
            completeCallbackId = FollowCallbackId.ADD_FOLLOWER_COMPLETE;
        }

        // user.following U {target}
        this.arrayModifier(user, target, FOLLOWING_ARRAY, true, true,
                String.format("successfully set %s to follow %s", user, target),
                String.format("failed to set %s to follow %s", user, target),
                counter,
                FollowCallbackId.ADD_USER_TO_FOLLOWING_FAIL,
                completeCallbackId);

        // target.followers U {user}
        this.arrayModifier(user, target, FOLLOWERS_ARRAY, false, true,
                String.format("successfully set %s to have %s as a follower", target, user),
                String.format("failed to set %s to have %s as a follower", target, user),
                counter,
                FollowCallbackId.ADD_USER_AS_FOLLOWER_FAIL,
                completeCallbackId);
    }

    /**
     * user =/=> target
     * (unfollow)
     */
    public void removeFollower(final String user, final String target) {
        removeFollower(user, target, null, null);
    }
    private void removeFollower(final String user, final String target, CompletedTaskCounter counter,
                                FollowCallbackId completeCallbackId) {
        if (counter == null) {
            counter = CompletedTaskCounter.getDefault();
        }
        if (completeCallbackId == null) {
            completeCallbackId = FollowCallbackId.REMOVE_FOLLOWER_COMPLETE;
        }

        // user.following \ {target}
        this.arrayModifier(user, target, FOLLOWING_ARRAY, true, false,
                String.format("successfully removed %s from following %s", user, target),
                String.format("failed to remove %s from following %s", user, target),
                counter,
                FollowCallbackId.REMOVE_FROM_FOLLOWING_FAIL,
                completeCallbackId);

        // target.followers \ {user}
        this.arrayModifier(user, target, FOLLOWERS_ARRAY, false, false,
                String.format("successfully removed %s from being a follower of %s", target, user),
                String.format("failed to remove %s from being a follower of %s", target, user),
                counter,
                FollowCallbackId.REMOVE_AS_FOLLOWER_FAIL,
                completeCallbackId);
    }

    /**
     * user -> target
     */
    public void sendFollowRequest(final String user, final String target) {
        sendFollowRequest(user, target, null, null);
    }
    private void sendFollowRequest(final String user, final String target, CompletedTaskCounter counter,
                                   FollowCallbackId completeCallbackId) {
        if (counter == null) {
            counter = CompletedTaskCounter.getDefault();
        }
        if (completeCallbackId == null) {
            completeCallbackId = FollowCallbackId.ADD_FOLLOW_REQUEST_COMPLETE;
        }

        // user.follow_requests_to U {target}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_TO_ARRAY, true, true,
                String.format("successfully uploaded pending follow request (%s -> %s)", user, target),
                String.format("failed to upload pending follow request (%s -> %s)", user, target),
                counter,
                FollowCallbackId.ADD_FOLLOW_REQUEST_TO_FAIL,
                completeCallbackId);

        // target.follow_requests_from U {user}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_FROM_ARRAY, false, true,
                String.format("successfully set %s to have a follow request from %s", target, user),
                String.format("failed to set %s to have a follow request from %s", target, user),
                counter,
                FollowCallbackId.ADD_FOLLOW_REQUEST_FROM_FAIL,
                completeCallbackId);
    }

    /**
     * user -/-> potentialFollowee
     * (also used for declining follow requests)
     */
    public void removeFollowRequest(final String user, final String target) {
        removeFollowRequest(user, target, null, null);
    }
    private void removeFollowRequest(final String user, final String target, CompletedTaskCounter counter,
                                     FollowCallbackId completeCallbackId) {
        if (counter == null) {
            counter = CompletedTaskCounter.getDefault();
        }
        if (completeCallbackId == null) {
            completeCallbackId = FollowCallbackId.REMOVE_FOLLOW_REQUEST_COMPLETE;
        }

        // user.follow_requests_to \ {target}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_TO_ARRAY, true, false,
                String.format("successfully removed %s's follow req from %s's follow req to array", target, user),
                String.format("failed to remove %s's follow req from %s's follow req to array", target, user),
                counter,
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_TO_FAIL,
                completeCallbackId);

        // target.follow_requests_from \ {user}
        this.arrayModifier(user, target, FOLLOW_REQUESTS_FROM_ARRAY, false, false,
                String.format("successfully removed %s's follow req from %s's follow req from array", user, target),
                String.format("failed to remove %s's follow req from %s's follow req from array", user, target),
                counter,
                FollowCallbackId.REMOVE_FOLLOW_REQUEST_FROM_FAIL,
                completeCallbackId);
    }

    /**
     * Note: A user accepting a follow request does two things:
     *   - target -/-> user
     *   - target => user
     */
    public void acceptFollowRequest(String user, String target) {
        int targetsAffected = 4;
        CompletedTaskCounter counter = new CompletedTaskCounter(targetsAffected);

        this.removeFollowRequest(target, user, counter, FollowCallbackId.ACCEPT_FOLLOW_REQUEST_COMPLETE);
        this.addFollower(target, user, counter, FollowCallbackId.ACCEPT_FOLLOW_REQUEST_COMPLETE);
    }

    /**
     * - gets all of the users that user is following & and for each followee, gets the most recent mood
     */
    public void getFollowingMoods(final String user) {
        final ArrayList<MoodView> followingMoods = new ArrayList<>();

        uc.getUserData(user, new UserController.CallbackUser() {
            @Override
            public void callbackUserData(DocumentSnapshot fetchedUserData, String callbackId) {
                final List<String> followingList = getListFromUser(fetchedUserData, FOLLOWING_ARRAY);

                // calls back an empty list user isn't following anyone
                if (followingList.size() == 0) {
                    ((OtherMoodsCallback) cc).callbackFollowingMoods(user, followingMoods);
                    return;
                }

                final CompletedTaskCounter counter = new CompletedTaskCounter(followingList.size());
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
                                    } else {
                                        // no mood (can happen if user's follower has no mood)
                                        if (task.getResult() != null ) {
                                            // even though it's a for loop,
                                            // it should still have at most 1 mood by the limit
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                Mood mood = Mood.fromDocSnapshot(doc);
                                                MoodView moodView = MoodView.fromMood(mood, followee);
                                                followingMoods.add(moodView);
                                            }
                                        }
                                    }

                                    // constantly checks whenever the counter is complete
                                    counter.incrementComplete();
                                    if (counter.isComplete()) {
                                        // sorts array by date after all moods are gotten
                                        // oldest -> newest
                                        Collections.sort(followingMoods, new Comparator<MoodView>() {
                                            @Override
                                            public int compare(MoodView o1, MoodView o2) {
                                                return o1.getDate().compareTo(o2.getDate());
                                            }
                                        });
                                        // reverses it to newest -> oldest
                                        Collections.reverse(followingMoods);

                                        ((OtherMoodsCallback) cc).callbackFollowingMoods(user, followingMoods);
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
    public void getFollowData(String user, String key) {
        getFollowData(user, key, (GetDataCallback) cc);
    }
    public void getFollowData(final String user, final String key, final GetDataCallback getDataCallback) {
        Log.d(TAG, "hello");
        uc.getUserSnapshot(user, key, new UserController.CallbackUserSnapshot() {
            @Override
            public void callbackUserSnapshot(@NonNull DocumentSnapshot fetchedUserData) {
                List<String> following = getListFromUser(fetchedUserData, FOLLOWING_ARRAY);
                List<String> followers = getListFromUser(fetchedUserData, FOLLOWERS_ARRAY);
                List<String> followRequestsFrom = getListFromUser(fetchedUserData, FOLLOW_REQUESTS_FROM_ARRAY);
                List<String> followRequestsTo = getListFromUser(fetchedUserData, FOLLOW_REQUESTS_TO_ARRAY);
                Log.d(TAG, "got following data for " + user);
                getDataCallback.callbackFollowData(user, following, followers, followRequestsFrom, followRequestsTo);
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
