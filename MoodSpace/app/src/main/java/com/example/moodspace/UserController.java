package com.example.moodspace;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

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

import io.paperdb.Paper;

import static com.example.moodspace.Utils.newUserBundle;

/**
 * Communicates user logins & signups between the UI and the firestore database
 */
public class UserController {
    private static final String TAG = UserController.class.getSimpleName();
    public static final String CHECK_USERNAME_EXISTS_KEY = "moodspace.UserController.checkUsernameExists";
    public static final String PAPER_USERNAME_KEY = "moodspace.Paper.username";
    public static final String PAPER_PASSWORD_KEY = "moodspace.Paper.password";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ControllerCallback cc;

    public UserController(ControllerCallback cc) {
        this.cc = cc;
    }

    public interface CallbackUser {
        void callbackUserData(DocumentSnapshot fetchedUserData, final String callbackId);
    }

    /*
    public void checkUserExists(final User user) {
        Query query = db.collection("users").whereEqualTo("username", user.getUsername());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() > 0) {
                    Log.d(TAG, "Username " + user.getUsername() + " is taken");
                    cc.callback(UserCallbackId.USERNAME_TAKEN);
                } else {
                    Log.d(TAG, "Username " + user.getUsername() + " is not taken");
                    cc.callback(UserCallbackId.USERNAME_NOT_TAKEN,
                            newUserBundle(SignUpActivity.SIGN_UP_USER_KEY, user));
                }
            }
        });
    }
     */

    /**
     * Checks if the username exists already in the database
     */
    public void checkUsernameExists(final String username, final Bundle bundle) {
        Query query = db.collection("users").whereEqualTo("username", username);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() > 0) {
                    Log.d(TAG, "Username " + username + " exists");
                    cc.callback(UserCallbackId.USERNAME_EXISTS, bundle);
                } else {
                    Log.d(TAG, "Username " + username + " does not exist");
                    cc.callback(UserCallbackId.USERNAME_DOESNT_EXIST, bundle);
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
                        cc.callback(UserCallbackId.LOGIN,
                                newUserBundle(LoginActivity.LOGIN_USER_KEY, user));
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error has occurred when adding user " + user.toString());
                        Log.d(TAG, Log.getStackTraceString(e));
                        cc.callback(UserCallbackId.USER_ADDITION_FAIL);
                    }
                });
    }


    /**
     * Fetches the user data from firestore given the username
     *
     * Requires UserController.CallbackUser interface to use
     */
    public void getUserData(String username, String callbackId) {
        getUserData(username, (UserController.CallbackUser) cc, callbackId);
    }

    /**
     * Fetches the user data from firestore given the username
     */
    public void getUserData(String username, UserController.CallbackUser ccu) {
        getUserData(username, ccu, null);
    }

    private void getUserData(final String username, final UserController.CallbackUser ccu, final String callbackId) {
        db.collection("users")
                .document(username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Error reading user data for user " + username);
                            Log.d(TAG, Log.getStackTraceString(task.getException()));
                            cc.callback(UserCallbackId.USER_READ_DATA_FAIL);
                            return;
                        }
                        if (task.getResult() == null) {
                            cc.callback(UserCallbackId.USER_TASK_NULL);
                            return;
                        }
                        if (!task.getResult().exists()) {
                            cc.callback(UserCallbackId.USER_NONEXISTENT);
                            return;
                        }

                        ccu.callbackUserData(task.getResult(), callbackId);

                    }
                });
    }

    /**
     * Logs in a user if the inputted password matches the fetched password
     *
     * @param inputtedUser inputted data from UI
     * @param userData fetched data from firestore
     */
    public void checkPassword(User inputtedUser, DocumentSnapshot userData) {
        String fetchedPassword = (String) userData.get("password");
        if (fetchedPassword == null) {
            cc.callback(UserCallbackId.PASSWORD_FETCH_NULL);
            return;
        }

        if (fetchedPassword.equals(inputtedUser.getPassword())) {
            cc.callback(UserCallbackId.LOGIN,
                    newUserBundle(LoginActivity.LOGIN_USER_KEY, inputtedUser));
        } else {
            cc.callback(UserCallbackId.INCORRECT_PASSWORD);
        }
    }

    public void rememberUser(User user) {
        Paper.book().write(PAPER_USERNAME_KEY, user.getUsername());
        Paper.book().write(PAPER_PASSWORD_KEY, user.getPassword());
    }
}
