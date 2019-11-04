package com.example.moodspace;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class AddEditController {
    private static final String TAG = AddEditController.class.getSimpleName();
    private Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage fbs = FirebaseStorage.getInstance();

    public AddEditController(Context context) {
        this.context = context;
    }

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

    /**
     * Gets photo path from image selector intent
     *
     * @return null if an error occurred, String if filepath
     */
    public String getPhotoPath(Intent data, ContentResolver contentResolver) {
        // converts uri to file path
        // https://www.viralpatel.net/pick-image-from-galary-android-app/
        Uri photoUri = data.getData();
        Log.d("EPIC", "" + photoUri);
        if (photoUri == null) {
            Toast.makeText(context, "Unexpected error: photo data shouldn't be null",
                    Toast.LENGTH_LONG).show();
            return null;
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = contentResolver.query(photoUri, filePathColumn,
                null, null, null);
        if (cursor == null) {
            Toast.makeText(context, "Unexpected error: photo cursor shouldn't be null",
                    Toast.LENGTH_LONG).show();
            return null;
        }
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String photoPath = cursor.getString(columnIndex);
        cursor.close();

        File photoFile = new File(photoPath);
        if (!photoFile.isFile()) {
            Toast.makeText(context, "Unexpected error: picture file path not found",
                    Toast.LENGTH_LONG).show();
            return null;
        }

        return photoPath;
    }

    public void uploadPhoto(String photoPath) {
        Bitmap src = BitmapFactory.decodeFile(photoPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        String path = "dank_meme.png";
        StorageReference imgRef = fbs.getReference(path);
        UploadTask uploadTask = imgRef.putBytes(data);
    }
}
