package com.example.moodspace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * This intent is used to add a new mood to the mood list, it takes in certain parameters and upon clicking the add mood, will create a new Mood object
 * and return it to the main activity, otherwise, it will catch any exceptions and notify the user.
 */
public class AddMood extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    private Toolbar toolbar;
    private String inputPhotoPath = null;
    private static final int GALLERY_PERMISSIONS_REQUEST = 1;
    private AddEditController aec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Mood");
        setSupportActionBar(toolbar);

        final String username = getIntent().getStringExtra("USERNAME");
        final Spinner spinnerEmotions = findViewById(R.id.emotionSelector);
        aec = new AddEditController(this);


        // sets up OK button
        // upon clicking the okay button, there will be an intent
        // to another activity to fill out the required information.
        final Button setMood = findViewById(R.id.saveBtn);
        setMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText reasonEditText = findViewById(R.id.reason_text);
                String reasonText;
                if (reasonEditText.getText() == null) {
                    reasonText = null;
                } else {
                    reasonText = reasonEditText.getText().toString();

                    // validates reasonText input (checks <= 3 words, 20 characters enforced by ui)
                    // https://stackoverflow.com/a/5864174
                    String trim = reasonText.trim();
                    if (!trim.isEmpty() && trim.split("\\s+").length > 3) {
                        Toast.makeText(AddMood.this, "Reason must be less than 4 words",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // currently set as string to prevent merge conflicts
                // might be better to store as UUID type?
                String id = UUID.randomUUID().toString();
                Date date = new Date();
                Emotion emotion = (Emotion) spinnerEmotions.getSelectedItem();
                boolean hasPhoto = (inputPhotoPath != null);

                // uploads mood before picture since it's more important to do so
                Mood mood = new Mood(id, date, emotion, reasonText, hasPhoto);
                aec.addMood(username, mood);
                if (hasPhoto) {
                    aec.uploadPhoto(inputPhotoPath, id);
                }

                finish();
            }
        });

        // creates emotion spinner
        List<Emotion> emotionList = Arrays.asList(Emotion.values());
        MoodAdapter mAdapter = new MoodAdapter(this, emotionList);
        spinnerEmotions.setAdapter(mAdapter);

        // TODO: social situation button dropdown
        final ImageButton socialSitbutton = findViewById(R.id.social_sit_button);
        socialSitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DEBUG: toggles visibility of image button
                /*
                Button imageButton = findViewById(R.id.image_button);
                if (imageButton.getVisibility() == View.GONE) {
                    imageButton.setVisibility(View.VISIBLE);
                } else {
                    imageButton.setVisibility(View.GONE);
                }
                 */

                Toast.makeText(AddMood.this, "Placeholder social situation",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // sets the select image intent to the image button
        final Button imageButton = findViewById(R.id.image_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // requests read permissions first (camera might be added later)
                // https://developer.android.com/training/permissions/requesting
                String[] galleryPermissions = {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };

                HashSet<String> necessaryPermissions = new HashSet<>();
                for (String permission: galleryPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            AddMood.this, permission) == PackageManager.PERMISSION_DENIED) {
                        necessaryPermissions.add(permission);
                    }
                }

                // actually requests permissions
                if (!necessaryPermissions.isEmpty()) {
                    ActivityCompat.requestPermissions(AddMood.this,
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
                AddMood.this.inputPhotoPath = null;

                imageButton.setVisibility(View.VISIBLE);
                removeImageButton.setVisibility(View.GONE);
                ImageView imageView = findViewById(R.id.image_view);
                imageView.setImageDrawable(null);
            }
        });

        // TODO location
        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddMood.this, "Placeholder map button",
                        Toast.LENGTH_SHORT).show();
            }
        });

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
            Log.d("EPIC", "start decode");
            Bitmap bm = BitmapFactory.decodeFile(inputPhotoPath);
            Log.d("EPIC", "finish decode");
            ImageView imageView = findViewById(R.id.image_view);
            imageView.setImageBitmap(bm);

            Button imageButton = findViewById(R.id.image_button);
            imageButton.setVisibility(View.GONE);
            ImageButton removeImageButton = findViewById(R.id.remove_image_button);
            removeImageButton.setVisibility(View.VISIBLE);

        }
    }
}
