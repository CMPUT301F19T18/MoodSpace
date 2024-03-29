package com.example.moodspace;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for modifying the mood event database
 */
public class MoodController {
    private static final String TAG = MoodController.class.getSimpleName();

    private ControllerCallback cc;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private CacheListener cacheListener = CacheListener.getInstance();

    public interface UserMoodsCallback {
        void callbackMoodList(@NonNull String user, @NonNull List<Mood> userMoodList);
    }

    public MoodController(ControllerCallback cc) {
        this.cc = cc;
    }

    public void addMood(String username, Mood newMood) {
        db.collection("users")
                .document(username)
                .collection("Moods")
                .document(newMood.getId())
                .set(newMood)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data addition successful");
                        cc.callback(MoodCallbackId.ADD_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data addition failed" + e.toString());
                        cc.callback(MoodCallbackId.ADD_FAIL);
                    }
                });
    }

    /**
     * Updates a mood with the given ID in said mood parameter with its other parameters
     */
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
                        cc.callback(MoodCallbackId.UPDATE_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data modification failed");
                        Log.d(TAG, Log.getStackTraceString(e));
                        cc.callback(MoodCallbackId.UPDATE_FAIL);
                    }
            });
    }


    public void deleteMood(String username, String moodId) {
        db.collection("users")
                .document(username)
                .collection("Moods")
                .document(moodId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Data deletion successful");
                        cc.callback(MoodCallbackId.DELETE_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data deletion failed:");
                        Log.d(TAG, Log.getStackTraceString(e));
                        cc.callback(MoodCallbackId.DELETE_FAIL);
                    }
                });
    }


    /**
     * gets a sorted (most to least recent) list of moods for any given user
     */
    public void getMoodList(String username, String key) {
        getMoodList(username, key, (UserMoodsCallback) this.cc);
    }
    public void getMoodList(final String username, final String key, final UserMoodsCallback userMoodsCallback) {
        ListenerRegistration registration
                = db.collection("users")
                .document(username)
                .collection("Moods")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable QuerySnapshot queryDocumentSnapshots,
                            @Nullable FirebaseFirestoreException e
                    ) {
                        if (e != null) {
                            Log.w(TAG, "Error: Mood list listen failed");
                            Log.w(TAG, Log.getStackTraceString(e));
                            return;
                        }
                        List<Mood> moodList = new ArrayList<>();
                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Mood mood = Mood.fromDocSnapshot(doc);
                                moodList.add(mood);
                            }
                        }
                        userMoodsCallback.callbackMoodList(username, moodList);
                    }
                });
        cacheListener.setListener(key, registration);

    }

    // TODO move elsewhere
    /**
     * Gets photo path from image selector intent
     *
     * @return null if an error occurred, String if filepath
     */
    public String getPhotoPath(Intent data, ContentResolver contentResolver) {
        // converts uri to file path
        // https://www.viralpatel.net/pick-image-from-galary-android-app/
        Uri photoUri = data.getData();
        if (photoUri == null) {
            //makeWarnToast(context, "Unexpected error: photo data shouldn't be null");
            return null;
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(photoUri, filePathColumn,
                null, null, null);
        if (cursor == null) {
            //makeWarnToast(context, "Unexpected error: photo cursor shouldn't be null");
            return null;
        }
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String photoPath = cursor.getString(columnIndex);
        cursor.close();

        File photoFile = new File(photoPath);
        if (!photoFile.isFile()) {
            //makeWarnToast(context, "Unexpected error: picture file path not found");
            return null;
        }

        return photoPath;
    }

    /**
     * uploads photo to firebase
     *
     * @param inputPhotoPath path locally on phone to photo
     * @param id mood event id
     */
    public UploadTask uploadPhoto(String inputPhotoPath, final String id) {
        Bitmap src = BitmapFactory.decodeFile(inputPhotoPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        String path = "mood_photos/" + id + ".png";
        StorageReference imgRef = fbStorage.getReference(path);
        UploadTask uploadTask = imgRef.putBytes(data);

        // adds a bunch of listeners for logging purposes
        // TODO: display on the UI
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred())
                        / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "progress of " + id + ": " + progress);
            }
        });

        return uploadTask;
    }

}
