package com.example.moodspace;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

/**
 * Communicates filtering options between the UI and the firestore database
 *
 * - Currently it stores filters as a collection under each user name "Filter" with each
 *   mood being stored as a key/value pair of itself. (eg. "Happy": "Happy")
 */
public class FilterController {
    private static final String TAG = FilterController.class.getSimpleName();
    private Emotion[] emotionArray = Emotion.values();
    private Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FilterController(Context context) {
        this.context = context;
    }

    /**
     * unchecks a filter from firestore
     * @param i the index of the emotion list
     */
    public void filterOut(int i, String username){
        db.collection("users")
                .document(username)
                .collection("Filter")
                .document(emotionArray[i].getEmojiName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data deletion successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data deletion failed" + e.toString());
                    }
                });
    }

    /**
     * unchecks a filter from firestore
     * @param i the index of the emotion list
     */
    public void filterIn(int i, String username) {
        emotionArray = Emotion.values();
        HashMap<String, Object> data = new HashMap<>();
        data.put("emotion", emotionArray[i]);
        db.collection("users")
                .document(username)
                .collection("Filter")
                .document(emotionArray[i].getEmojiName())
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
