package com.example.moodspace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

/**
 * Activity for adding / editing moods
 * - editing is also used to view the details of your own moods
 */
public class AddEditActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {
    private static final int PICK_IMAGE = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 1;
    // TODO: change back down to 8 when jpgs can be uploaded properly
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;
    private static final String TAG = AddEditActivity.class.getSimpleName();

    private AddEditController aec;

    // can be null if reusing a downloaded photo while editing
    private String inputPhotoPath = null;
    private boolean hasPhoto = false;
    private boolean changedPhoto = false;
    private Mood currentMood = null;
    private Emotion selectedEmotion = null;

    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();

    //Location variables
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 110;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean attachLocation = true;

    /**
     * Initializes all input methods for adding a mood.
     * - If editing a mood, it takes its parameters and puts it in said input methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_add_edit_mood);

        setupMapView(savedInstanceState);

        final String username = getIntent().getStringExtra("USERNAME");
        aec = new AddEditController(this);
        currentMood = (Mood) getIntent().getSerializableExtra("MOOD");

        if(isAddActivity()){
            mMapView.setVisibility(MapView.GONE);
        }
        else{
            CheckBox locCheck = findViewById(R.id.checkbox_location);
            locCheck.setVisibility(CheckBox.GONE);
        }

        // creates emotion spinner
        final Spinner emotionSpinner = findViewById(R.id.emotionSelector);
        List<Emotion> emotionList = Arrays.asList(Emotion.values());
        // last argument is initialTextWasShown (true if EditActivity, false if AddActivity)
        final EmotionAdapter emotionAdapter = new EmotionAdapter(this, emotionList);
        emotionSpinner.setAdapter(emotionAdapter);
        emotionSpinner.setOnItemSelectedListener(this);

        // creates social situation spinner
        final Spinner socialSitSpinner = findViewById(R.id.situationSelector);
        List<SocialSituation> socialSitList = Arrays.asList(SocialSituation.values());
        SocialSituationAdapter socialSituationAdapter = new SocialSituationAdapter(this, socialSitList);
        socialSitSpinner.setAdapter(socialSituationAdapter);

        // sets up save button
        // upon clicking the okay button, there will be an intent
        // to another activity to fill out the required information.
        final Button saveBtn = findViewById(R.id.saveBtn);
        final TextInputEditText reasonEditText = findViewById(R.id.reason_text);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // disables so you can't click multiple times
                saveBtn.setEnabled(false);

                // requires an emotion to be selected
                if (selectedEmotion == null) {
                    Toast.makeText(AddEditActivity.this, "Select an emotion", Toast.LENGTH_SHORT).show();
                    saveBtn.setEnabled(true);
                    return;
                }

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
                        saveBtn.setEnabled(true);
                        return;
                    }
                }

                String id;
                Date date;
                boolean hasPhoto = AddEditActivity.this.hasPhoto;
                SocialSituation socialSit = (SocialSituation) socialSitSpinner.getSelectedItem();
                double lat = -1000;
                double lon = -1000;

                // reuses parameters if editing
                if (isAddActivity()) {
                    id = UUID.randomUUID().toString();
                    date = new Date();

                    if(attachLocation) {
                        Location loc = getDeviceLocation();
                        lat = loc.getLatitude();
                        lon = loc.getLongitude();
                    }



                } else {
                    //TODO Display message location not provided instead of empty map (Maybe)
                    id = currentMood.getId();
                    date = currentMood.getDate();
                    lat = currentMood.getLat();
                    lon = currentMood.getLon();
                }

                Mood mood = new Mood(id, date, selectedEmotion, reasonText, hasPhoto, socialSit, lat, lon);
                if (isAddActivity()) {
                    aec.addMood(username, mood);

                } else {
                    aec.updateMood(username, mood);
                }

                // only uploads if the photo hasn't changed for optimization purposes
                if (hasPhoto && changedPhoto) {
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

        // makes sure views are displayed as normal
        this.removePreviewImage();

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
                removePreviewImage();
                inputPhotoPath = null;
            }
        });

        // TODO See for map stuff if want the ability to remove attached location in edit mood


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
            // adds for it to work with "please select emotion" position
            int emotionIndex = emotionAdapter.getPosition(currentMood.getEmotion()) + 1;
            emotionSpinner.setSelection(emotionIndex);
            int socialSitIndex = socialSituationAdapter.getPosition(currentMood.getSocialSituation());
            socialSitSpinner.setSelection(socialSitIndex);
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

            // displays date and time
            TextView dateInfo = findViewById(R.id.date);
            TextView timeInfo = findViewById(R.id.time);
            String parsedDate = Utils.formatDate(currentMood.getDate());
            String parsedTime = Utils.formatTime(currentMood.getDate());
            dateInfo.setText(parsedDate);
            timeInfo.setText(parsedTime);
        }

        setSupportActionBar(toolbar);
    }

    private Location getDeviceLocation(){
        // TODO: fix app crash if user denies location permission
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions(AddEditActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

        }

        while(true) {

            try {
                Location location = locationManager.getLastKnownLocation(locationManager
                        .getBestProvider(criteria, false));
                return location;
            }
            catch(Exception ex){ continue;}
        }




    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_location:
                if (checked){
                    attachLocation = true;
                }

            else{
                    attachLocation = false;
                }

                break;

        }
    }

    private void setupMapView(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(AddEditActivity.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        else if (requestCode == MY_PERMISSIONS_REQUEST_FINE_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {

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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // subtracts by one to work with "please select an emotion"
        if (i == 0) {
            return;
        }
        selectedEmotion = (Emotion) adapterView.getItemAtPosition(i - 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //TODO: right zoom level or the target button to do that
        double lat = -1000;
        double lon = -1000;

        Location loc = getDeviceLocation();

        googleMap.setMyLocationEnabled(true);

        if (this.isAddActivity()) {
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        } else {
            lat = currentMood.getLat();
            lon = currentMood.getLon();

            if(lat != -1000){

                LatLng sydney = new LatLng(lat,lon);
                googleMap.addMarker(new MarkerOptions().position(sydney));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        }

    }
}
