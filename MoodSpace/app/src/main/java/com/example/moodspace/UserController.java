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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class UserController {
    private static final String TAG = UserController.class.getSimpleName();
    private Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Emotion> emotionList;

    public UserController(Context context) {
        this.context = context;
    }

    public void checkUserExists(User user) {
        final User enteredUser = user;
        Query query = db.collection("users").whereEqualTo("username", user.getUsername());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() > 0) {
                    Toast.makeText(context, "This username is taken", Toast.LENGTH_SHORT).show();
                } else {
                    //add a new user to Firestore database
                    signUpUser(enteredUser);
                }
            }
        });
    }

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
                        Intent i = new Intent(context, ListActivity.class);
                        // TODO: pass user to activity
                        i.putExtra("Username",username);
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
        emotionList = Arrays.asList(Emotion.values());
        HashMap<String, String> data = new HashMap<>();
        for (int i = 0; i < emotionList.size(); i++) {
            data.put(username, emotionList.get(i).getEmojiString());
            db.collection("users")
                    .document(username)
                    .collection("Filter")
                    .document(emotionList.get(i).getEmojiName())
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
                            if (task.getResult().exists()) {
                                if (task.getResult().get("password").equals(password)) {
                                    Intent i = new Intent(context, ListActivity.class);
                                    i.putExtra("Username",username);

                                    // TODO: pass user to activity
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
