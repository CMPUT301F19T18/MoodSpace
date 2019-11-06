package com.example.moodspace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AddEditActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 1;
    // TODO: change back down to 8 when jpgs can be uploaded properly
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;
    private static final String TAG = AddEditActivity.class.getSimpleName();
    // can be null if reusing a downloaded photo while editing
    private String inputPhotoPath = null;
    private boolean hasPhoto = false;
    private boolean changedPhoto = false;
    private AddEditController aec;
    private Mood currentMood = null;
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_mood);

        final String username = getIntent().getStringExtra("USERNAME");
        final Spinner spinnerEmotions = findViewById(R.id.emotionSelector);
        aec = new AddEditController(this);
        currentMood = (Mood) getIntent().getSerializableExtra("MOOD");

        // sets up save button
        // upon clicking the okay button, there will be an intent
        // to another activity to fill out the required information.
        final Button saveBtn = findViewById(R.id.saveBtn);
        final TextInputEditText reasonEditText = findViewById(R.id.reason_text);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reasonText;
                if (reasonEditText.getText() == null) {
                    reasonText = null;
                } else {
                    reasonText = reasonEditText.getText().toString();

                    // validates reasonText input (checks <= 3 words, 20 characters enforced by ui)
                    // https://stackoverflow.com/a/5864174
                    String trim = reasonText.trim();
                    if (!trim.isEmpty() && trim.split("\\s+").length > 3) {
                        Toast.makeText(AddEditActivity.this, "Reason must be less than 4 words",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                String id;
                Date date;
                boolean hasPhoto = AddEditActivity.this.hasPhoto;
                Emotion emotion = (Emotion) spinnerEmotions.getSelectedItem();

                // reuses parameters if editing
                if (AddEditActivity.this.isAddActivity()) {
                    id = UUID.randomUUID().toString();
                    date = new Date();
                } else {
                    id = currentMood.getId();
                    date = currentMood.getDate();
                }

                Mood mood = new Mood(id, date, emotion, reasonText, hasPhoto);
                if (AddEditActivity.this.isAddActivity()) {
                    aec.addMood(username, mood);
                } else {
                    aec.updateMood(username, mood);
                }

                // only uploads if the photo hasn't changed for optimization purposes
                if (hasPhoto && AddEditActivity.this.changedPhoto) {
                    aec.uploadPhoto(inputPhotoPath, id);
                }

                finish();
            }
        });

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // creates emotion spinner
        List<Emotion> emotionList = Arrays.asList(Emotion.values());
        MoodAdapter mAdapter = new MoodAdapter(this, emotionList);
        spinnerEmotions.setAdapter(mAdapter);
//        spinnerEmotions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (parent.getItemAtPosition(position).equals("NULL")) {
//                    // do nothing
//                }
//                else {
//                    //on Selecting a spinner item
//                    Emotion emotion = parent.getItemAtPosition(position)
//
//                    //Show selected spinner item
//                    Toast.makeText(parent.get)
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedItemText = (String) parent.getItemAtPosition(position);
//                // If user change the default selection
//                // First item is disable and it is used for hint
//                if(position > 0){
//                    // Notify the selected item text
//                    Toast.makeText
//                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
//                            .show();
//                }
//            }
        // TODO: social situation button dropdown
        final ImageButton socialSitbutton = findViewById(R.id.social_sit_button);
        socialSitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddEditActivity.this, "Placeholder social situation",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // sets the select image intent to the image button
        final Button imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // requests read permissions first (camera might be added later)
                String[] galleryPermissions = {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

                HashSet<String> necessaryPermissions = new HashSet<>();
                for (String permission: galleryPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            AddEditActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
                        necessaryPermissions.add(permission);
                    }
                }

                // actually requests permissions
                if (!necessaryPermissions.isEmpty()) {
                    ActivityCompat.requestPermissions(AddEditActivity.this,
                            necessaryPermissions.toArray(new String[0]),
                            GALLERY_PERMISSIONS_REQUEST);
                    return;
                }

                // otherwise, has all permissions
                createImageIntent();
            }
        });

        // sets up removes image button
        // note: only active if there is an image
        final ImageButton removeImageButton = findViewById(R.id.remove_image_button);
        removeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddEditActivity.this.removePreviewImage();
                AddEditActivity.this.inputPhotoPath = null;
            }
        });

        // TODO location
        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddEditActivity.this, "Placeholder map button",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        // sets up add/edit specific attributes
        if (this.isAddActivity()) {
            toolbar.setTitle("Add Mood");
            saveBtn.setText(getString(R.string.am_ok_text));
            backBtn.setText(getString(R.string.am_cancel_text));
        } else {
            toolbar.setTitle("Edit Mood");
            saveBtn.setText(getString(R.string.em_ok_text));
            backBtn.setText(getString(R.string.em_cancel_text));

            // fills in fields with previous values
            int emotionIndex = mAdapter.getPosition(currentMood.getEmotion());
            spinnerEmotions.setSelection(emotionIndex);
            reasonEditText.setText(currentMood.getReasonText());

            // downloads photo: can't figure out how to separate this task into the controller
            // Create a storage reference from our app
            if (currentMood.getHasPhoto()) {
                String path = "mood_photos/" + currentMood.getId() + ".png";
                StorageReference storageRef = fbStorage.getReference().child(path);

                storageRef.getBytes(MAX_DOWNLOAD_LIMIT).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        AddEditActivity.this.setPreviewImage(bm, false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: retry
                        Toast.makeText(AddEditActivity.this, "Failed to load existing photo",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        setSupportActionBar(toolbar);
    }

    private boolean isAddActivity() {
        return this.currentMood == null;
    }

    /**
     * Called when permissions are accepted/denied after the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == GALLERY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createImageIntent();
            } else {
                Toast.makeText(this, "Cannot access photos",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createImageIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    /**
     * Called when a photo is selected (leaving the activity)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            this.inputPhotoPath = aec.getPhotoPath(data, getContentResolver());

            // TODO: preview image in add/edit
            Log.d(TAG, "start decode");
            Bitmap bm = BitmapFactory.decodeFile(inputPhotoPath);
            Log.d(TAG, "finish decode");
            this.setPreviewImage(bm, true);

        }
    }

    private void removePreviewImage() {
        ImageView imageView = findViewById(R.id.image_view);
        Button imageButton = findViewById(R.id.image_button);
        ImageButton removeImageButton = findViewById(R.id.remove_image_button);

        imageView.setImageDrawable(null);
        imageButton.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.GONE);

        this.hasPhoto = false;
    }

    private void setPreviewImage(Bitmap bm, boolean changedPhoto) {
        ImageView imageView = findViewById(R.id.image_view);
        Button imageButton = findViewById(R.id.image_button);
        ImageButton removeImageButton = findViewById(R.id.remove_image_button);

        imageView.setImageBitmap(bm);
        imageButton.setVisibility(View.GONE);
        removeImageButton.setVisibility(View.VISIBLE);

        // if ever true, then changedPhoto is true
        this.changedPhoto |= changedPhoto;
        this.hasPhoto = true;
    }
}
