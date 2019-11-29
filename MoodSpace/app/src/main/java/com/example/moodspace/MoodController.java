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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
    private static final String TAG = "EPIC";

    private ControllerCallback cc;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();

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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data addition failed" + e.toString());
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data modification failed" + e.toString());
                    }
            });
    }

    /**
     * gets a sorted (most to least recent) list of moods for any given user
     */
    public void getMoodList(String username) {
        getMoodList(username, (UserMoodsCallback) this.cc);
    }
    public void getMoodList(final String username, final UserMoodsCallback userMoodsCallback) {
        db.collection("users")
                .document(username)
                .collection("Moods")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable QuerySnapshot queryDocumentSnapshots,
                            @Nullable FirebaseFirestoreException e
                    ) {
                        List<Mood> moodList = new ArrayList<>();
                        if (queryDocumentSnapshots != null) {
                            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                String id = dc.getDocument().getId();
                                switch (dc.getType()) {
                                    case ADDED:
                                        Log.d(TAG, "added " + id);
                                    case MODIFIED:
                                        Log.d(TAG, "modified " + id);
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "removed " + id);
                                        break;
                                    default:
                                        Log.d(TAG, "wtf " + id);
                                        break;
                                }
                            }

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                Mood mood = Mood.fromDocSnapshot(doc);
                                moodList.add(mood);
                            }
                        }
                        userMoodsCallback.callbackMoodList(username, moodList);
                    }
                });
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
