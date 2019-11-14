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

    public static final String USERNAME_TAKEN = "username taken";
    public static final String USERNAME_NOT_TAKEN = "username not taken";
    public static final String USERNAME_NONEXISTENT = "username doesn't exist";
    public static final String LOGIN = "successful login";
    public static final String LOGIN_READ_FAIL = "login read fail";
    public static final String INCORRECT_PASSWORD = "incorrect password";
    public static final String PASSWORD_TASK_NULL = "password task result null";
    public static final String PASSWORD_FETCH_NULL = "password fetch null";
    public static final String USER_ADDITION_FAIL = "user addition fail";
    public static final String FILTER_INITIALIZE_FAIL = "filter initialize fail";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;


    public UserController(ControllerCallback cc) {
        this.cc = cc;
    }

    /**
     * Used to ensure all users have a unique username
     */
    public void checkUserExists(final User user) {
        Query query = db.collection("users").whereEqualTo("username", user.getUsername());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() > 0) {
                    Log.d(TAG, "Username " + user.getUsername() + " is taken");
                    cc.callback(USERNAME_TAKEN);
                } else {
                    Log.d(TAG, "Username " + user.getUsername() + " is not taken");
                    cc.callback(USERNAME_NOT_TAKEN);
                    //signUpUser(user);
                }
            }
        });
    }

    /**
     * Signs up a user by creating a user entry in firebase
     * - Also creates default filter values for each user
     */
    public void signUpUser(final User user) {
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
                        cc.callback(LOGIN);
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error has occurred when adding user " + user.toString());
                        Log.d(TAG, Log.getStackTraceString(e));
                        cc.callback(USER_ADDITION_FAIL);
                    }
                });

        // create the default filter with all emotions
        HashMap<String, Object> data = new HashMap<>();
        for (final Emotion emotion : Emotion.values()) {
            data.put("emotion", emotion);
            db.collection("users")
                    .document(username)
                    .collection("Filter")
                    .document(emotion.getEmojiName())
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Filter " + emotion.getEmojiName() + " was successfully added");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error has occurred when adding filter " + emotion.getEmojiName());
                            Log.d(TAG, Log.getStackTraceString(e));
                            cc.callback(FILTER_INITIALIZE_FAIL);
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
    public void loginUser(final User user) {
        final String username = user.getUsername();
        final String password = user.getPassword();

        CollectionReference collectionReference = db.collection("users");
        collectionReference
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Error reading user data when logging in for user " + user.toString());
                            Log.d(TAG, Log.getStackTraceString(task.getException()));
                            cc.callback(LOGIN_READ_FAIL);
                            return;
                        }
                        if (task.getResult() == null) {
                            cc.callback(PASSWORD_TASK_NULL);
                            return;
                        }
                        if (!task.getResult().exists()) {
                            cc.callback(USERNAME_NONEXISTENT);
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
                });
    }
}
