package com.example.moodspace;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
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

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Context context;

    public FollowController(Context context) {
        this.context = context;
    }

    /**
     * user => target
     */
    public Task<Void> addFollower(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(user);

        return doc.update(FOLLOWING_ARRAY, FieldValue.arrayUnion(target))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("add follower success (%s => %s)", user, target));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to add follower (%s => %s)", user, target));
                        Toast.makeText(context,
                                "Error: Failed to follow " + target, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * user =/=> target
     * (unfollow)
     */
    public Task<Void> removeFollower(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(user);

        return doc.update(FOLLOWING_ARRAY, FieldValue.arrayRemove(target))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("remove follower success (%s => %s)", user, target));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to remove follower (%s => %s)", user, target));
                        Toast.makeText(context,
                                "Error: Failed to unfollow " + target, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * user -> target
     */
    public Task<Void> sendFollowRequest(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(target);

        return doc.update(FOLLOW_REQUESTS_FROM_ARRAY, FieldValue.arrayUnion(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("add follow request success (%s -> %s)",
                                user, target));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to add follow request (%s -> %s)",
                                user, target));
                        Toast.makeText(context,
                                "Error: Failed to send follow request to " + target,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * user -/-> potentialFollowee
     * (also used for declining follow requests)
     */
    public Task<Void> removeFollowRequest(final String user, final String target) {
        final DocumentReference doc = db.collection("users").document(target);

        return doc.update(FOLLOW_REQUESTS_FROM_ARRAY, FieldValue.arrayRemove(user))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, String.format("remove follow request success (%s -> %s)",
                                user, target));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, String.format("failed to remove follow request (%s => %s)",
                                user, target));
                        Toast.makeText(context,
                                "Error: Failed to remove follow request to " + target,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public List<MoodOther> getFollowingMoods(String user) {
        return null;
    }


}
