package com.example.moodspace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * Communicates user logins & signups between the UI and the firestore database
 */
public class UserController {
    private static final String TAG = UserController.class.getSimpleName();
    public static final String USERNAME_KEY = "moodspace.UserController.username";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Context context;


    public UserController(Context context) {
        this.context = context;
    }

    /**
     * Used to ensure all users have a unique username
     */
    public void checkUserExists(User user) {
        final User enteredUser = user;
        Query query = db.collection("users").whereEqualTo("username", user.getUsername());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() > 0) {
                    Toast.makeText(context, "This username is taken", Toast.LENGTH_SHORT).show();
                } else {
                    // add a new user to FireStore database
                    signUpUser(enteredUser);
                }
            }
        });
    }

    /**
     * Signs up a user by creating a user entry in firebase
     * - Also creates default filter values for each user
     */
    public void signUpUser(User user) {
        final String username = user.getUsername();
        String password = user.getPassword();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("password", password);

        final CollectionReference collectionReference = db.collection("users");
        collectionReference
                .document(username)
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User was successfully added");
                        Intent i = new Intent(context, ProfileListActivity.class);
                        i.putExtra(USERNAME_KEY, username);
                        context.startActivity(i);
                        ((Activity) context).finish();
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error has occurred " + e.getMessage());
                    }
                });

        // create the default filter with all emotions
        HashMap<String, Object> data = new HashMap<>();
        for (Emotion emotion : Emotion.values()) {
            data.put("emotion", emotion);
            db.collection("users")
                    .document(username)
                    .collection("Filter")
                    .document(emotion.getEmojiName())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Data addition successful");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Data addition failed" + e.toString());
                        }
                    });
        }
    }

    /**
     * Logs in a user by checking with firebase to see if the username & password matches
     *
     * Possible errors that could occur:
     * - username not found
     * - password is wrong
     */
    public void loginUser(User user) {
        final String username = user.getUsername();
        final String password = user.getPassword();

        CollectionReference collectionReference = db.collection("users");
        collectionReference
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() == null) {
                                Toast.makeText(context,
                                        "Unexpected error: password task result should not be null",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (task.getResult().exists()) {
                                String fetchedPassword = (String) task.getResult().get("password");
                                if (fetchedPassword == null) {
                                    Toast.makeText(context,
                                            "Unexpected error: fetched password should not be null",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (fetchedPassword.equals(password)) {
                                    Intent i = new Intent(context, ProfileListActivity.class);
                                    i.putExtra(USERNAME_KEY, username);
                                    context.startActivity(i);
                                    ((Activity) context).finish();
                                } else {
                                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "This username does not exist", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            String ex = "";
                            if (task.getException() != null) {
                                ex = task.getException().getMessage();
                            }
                            Log.d(TAG, "Error reading user data " + ex);
                        }
                    }
                });
    }
}
