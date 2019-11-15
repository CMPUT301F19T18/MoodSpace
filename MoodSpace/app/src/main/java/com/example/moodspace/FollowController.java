package com.example.moodspace;

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

import java.util.List;

/**
 * Followers can be thought of as a directed graph between users.
 * Both requests and following are represented in firestore as adjacency lists under:
 *   users -> (username) -> Following
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
 */
public class FollowController {
    private static final String TAG = FollowController.class.getSimpleName();
    private static final String FOLLOWING_ARRAY = "Following";
    private static final String FOLLOW_REQUESTS_FROM_ARRAY = "FollowRequestsFrom";

    public static final String ADD_FOLLOWER_SUCCESS = "add follower success";
    public static final String ADD_FOLLOWER_FAIL = "add follower failure";
    public static final String REMOVE_FOLLOWER_SUCCESS = "remove follower success";
    public static final String REMOVE_FOLLOWER_FAIL = "remove follower failure";
    public static final String SEND_REQUEST_SUCCESS = "send follow request success";
    public static final String SEND_REQUEST_FAIL = "send follow request fail";
    public static final String REMOVE_REQUEST_SUCCESS = "remove follow request success";
    public static final String REMOVE_REQUEST_FAIL = "remove follow request fail";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;

    public FollowController(ControllerCallback cc) {
        this.cc = cc;
    }

    public interface Callback {
        void callbackFollowingMoods(List<MoodOther> followingMoodsList);
    }

    /**
     * user => target
     */
    public void addFollower(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(user);

        doc.update(FOLLOWING_ARRAY, FieldValue.arrayUnion(target))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("add follower success (%s => %s)", user, target));
                        cc.callback(ADD_FOLLOWER_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to add follower (%s => %s)", user, target));
                        cc.callback(ADD_FOLLOWER_FAIL);
                    }
                });
    }

    /**
     * user =/=> target
     * (unfollow)
     */
    public void removeFollower(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(user);

         doc.update(FOLLOWING_ARRAY, FieldValue.arrayRemove(target))
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
     * gets all of the users that user is following, and for each user, gets the most recent mood
     */
    public void getFollowingMoods(String user) {
        final DocumentReference doc = db.collection("users").document(user);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "Error reading user data when logging in for user " + user.toString());
                    Log.d(TAG, Log.getStackTraceString(task.getException()));
                    cc.callback(GET_USER_FAIL);
                    return;
                }
                if (task.getResult() == null) {
                    cc.callback(USER_TASK_NULL);
                    return;
                }
                if (!task.getResult().exists()) {
                    cc.callback(USER_NONEXISTENT);
                    return;
                }

                String fetchedPassword = (String) task.getResult().get("password");
                if (fetchedPassword == null) {
                    cc.callback(PASSWORD_FETCH_NULL);
                    return;
                }
                if (fetchedPassword.equals(password)) {
                    cc.callback(LOGIN);
                } else {
                    cc.callback(INCORRECT_PASSWORD);
                }

            }
        })


        ((Callback) cc).callbackFollowingMoods(null);

    }
}
