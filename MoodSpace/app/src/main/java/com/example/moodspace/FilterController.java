package com.example.moodspace;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class FilterController {
    private boolean ok = true;
    private boolean isCheck;
    private static final String TAG = FilterController.class.getSimpleName();
    private List<Emotion> emotionList;
    Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FilterController(Context context) {
        this.context = context;
    }

    public void filterOut(int i, String username){
        emotionList = Arrays.asList(Emotion.values());
        db.collection("users")
                .document(username)
                .collection("Filter")
                .document(emotionList.get(i).getEmojiName())
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

    public void filterIn(int i, String username) {
        emotionList = Arrays.asList(Emotion.values());
        HashMap<String, Object> data = new HashMap<>();
        data.put("emotion", emotionList.get(i));
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
