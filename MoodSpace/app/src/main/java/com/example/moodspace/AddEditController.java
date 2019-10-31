package com.example.moodspace;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEditController {
    private static final String TAG = AddEditController.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addMood(String username, Mood newMood) {
        db.collection("users")
                .document(username)
                .collection("Moods")
                .document()
                .set(newMood)
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

    public void updateMood(String username, Mood updatedMood) {
        db.collection("users")
                .document(username)
                .collection("Moods")
                .document(updatedMood.getId())
                .set(updatedMood)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data modification failed" + e.toString());
                    }
            });
    }
}
