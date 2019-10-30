package com.example.moodspace;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditController {
    private static final String TAG = AddEditController.class.getSimpleName();
    private Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AddEditController(Context context) {
        this.context = context;
    }

    public void addMood(String username, Mood newMood) {
        Map<String, Object> mood = new HashMap<>();
        mood.put("emotion", newMood.getEmotion());
        mood.put("time", newMood.getDateTime());
        db.collection("users")
                .document(username)
                .collection("Moods")
                .document()
                .set(mood)
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
