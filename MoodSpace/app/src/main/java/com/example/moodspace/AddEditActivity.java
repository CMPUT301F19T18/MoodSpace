package com.example.moodspace;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import static com.example.moodspace.Utils.makeInfoToast;
import static com.example.moodspace.Utils.makeWarnToast;

/**
 * Activity for adding / editing moods
 * - editing is also used to view the details of your own moods
 */
public class AddEditActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, OnMapReadyCallback,
        ControllerCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "moodspace.AddEditActivity.mapViewBundleKey";

    private static final int PICK_IMAGE = 1;
    private static final int GALLERY_PERMISSIONS_REQUEST = 1;
    private static final int FINE_LOCATION_PERMISSIONS_REQUEST = 2;
    // TODO: change back down to 8 when jpgs can be uploaded properly
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;
    private static final String TAG = AddEditActivity.class.getSimpleName();

    private MoodController mc;
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();

    // can be null if reusing a downloaded photo while editing
    private String username;
    private String inputPhotoPath = null;
    private boolean hasPhoto = false;
    private boolean changedPhoto = false;
    private Mood currentMood = null;
    private Emotion selectedEmotion = null;

    private TextInputEditText reasonEditText;
    private CheckBox locationCheckBox;
    private Spinner socialSitSpinner;
    private Button saveBtn;

    // location variables
    private GoogleMap gMap;
    private MapView mapView;
    private LocationCallback locationCallback;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation = null;
    private Marker currentMarker;
    private AlertDialog gpsAlert;
    private AlertDialog locationAlert;

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

        username = getIntent().getStringExtra(Utils.USERNAME_KEY);
        mc = new MoodController(this);
        currentMood = (Mood) getIntent().getSerializableExtra("MOOD");

        // gets all necessary views
        reasonEditText = findViewById(R.id.reason_text);
        locationCheckBox = findViewById(R.id.locationCheckbox);
        socialSitSpinner = findViewById(R.id.situationText);
        saveBtn = findViewById(R.id.saveBtn);

        // creates emotion spinner
        final Spinner emotionSpinner = findViewById(R.id.emotionSelector);
        List<EmotionWithNull> emotionWithNullList = Arrays.asList(EmotionWithNull.values());
        // last argument is initialTextWasShown (true if EditActivity, false if AddActivity)
        final EmotionAdapter emotionAdapter = new EmotionAdapter(this, emotionWithNullList);
        emotionSpinner.setAdapter(emotionAdapter);
        emotionSpinner.setOnItemSelectedListener(this);

        // creates social situation spinner
        List<SocialSituation> socialSitList = Arrays.asList(SocialSituation.values());
        SocialSituationAdapter socialSituationAdapter = new SocialSituationAdapter(this, socialSitList);
        socialSitSpinner.setAdapter(socialSituationAdapter);

        // sets up save button
        // upon clicking the okay button, there will be an intent
        // to another activity to fill out the required information.
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSaveMood(false);
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
                AddEditActivity.this.removePreviewImage();
                AddEditActivity.this.inputPhotoPath = null;
            }
        });

        // TODO See for map stuff if want the ability to remove attached location in edit mood
        // TODO move to controller?
        // sets up map stuff
        final TextView placeholderText = findViewById(R.id.placeholder_msg);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "location result success");
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "location result: " + location);
                    if (location != null) {
                        updateCurrentLocation(location);
                    }
                }
            }
        };

        // sets up checkbox button
        locationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean attachLocation = locationCheckBox.isChecked();
                if (isAddActivity()) {
                    if (attachLocation) {
                        placeholderText.setVisibility(View.GONE);
                        mapView.setVisibility(View.VISIBLE);

                        // attempts to grant the permission if not granted yet
                        boolean locationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED;
                        if (!locationPermission) {
                            // requests permission
                            ActivityCompat.requestPermissions(AddEditActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    FINE_LOCATION_PERMISSIONS_REQUEST);
                            return;
                        }
                        // gets location here since location permission is granted
                        // https://stackoverflow.com/a/10917500
                        startGettingLocation();

                    } else {
                        placeholderText.setVisibility(View.VISIBLE);
                        mapView.setVisibility(View.GONE);
                        stopGettingLocation();
                    }
                } else { // edit activity
                    if (attachLocation) {
                        mapView.setVisibility(View.VISIBLE);
                        placeholderText.setVisibility(View.GONE);
                    } else {
                        mapView.setVisibility(View.GONE);
                        placeholderText.setVisibility(View.VISIBLE);
                    }
                    stopGettingLocation();
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);


        // sets up add/edit specific attributes
        if (this.isAddActivity()) {
            toolbar.setTitle("Add Mood");
            saveBtn.setText(getString(R.string.am_ok_text));
            backBtn.setText(getString(R.string.am_cancel_text));

            // no map as there is no location set currently
            mapView.setVisibility(View.GONE);
        } else {
            toolbar.setTitle("Edit Mood");
            saveBtn.setText(getString(R.string.em_ok_text));
            backBtn.setText(getString(R.string.em_cancel_text));


            // fills in fields with previous values
            // adds for it to work with "please select emotion" position
            int emotionIndex = emotionAdapter.getPosition(currentMood.getEmotion().toEmotionWithNull()) + 1;
            emotionSpinner.setSelection(emotionIndex);
            int socialSitIndex = socialSituationAdapter.getPosition(currentMood.getSocialSituation());
            socialSitSpinner.setSelection(socialSitIndex);
            reasonEditText.setText(currentMood.getReasonText());

            final TextView locationText = findViewById(R.id.locationText);
            final View rightBox = findViewById(R.id.right_square_view);

            // displays the location checkbox if a location was already stored
            if (currentMood.getLat() != null && currentMood.getLon() != null) {
                locationCheckBox.setVisibility(View.VISIBLE);

                if (currentMood.getHasLocation()) {
                    locationCheckBox.setChecked(true);
                    placeholderText.setVisibility(View.GONE);
                } else {
                    mapView.setVisibility(View.GONE);
                    placeholderText.setVisibility(View.VISIBLE);
                }
            } else {
                // no location at all: hides all
                mapView.setVisibility(View.GONE);
                placeholderText.setVisibility(View.GONE);
                locationText.setVisibility(View.GONE);
                rightBox.setVisibility(View.GONE);
                locationCheckBox.setVisibility(View.GONE);
            }

            // downloads photo: can't figure out how to separate this task into the controller
            // Create a storage reference from our app
            if (currentMood.getHasPhoto()) {
                String path = "mood_photos/" + currentMood.getId() + ".png";
                StorageReference storageRef = fbStorage.getReference().child(path);

                storageRef
                        .getBytes(MAX_DOWNLOAD_LIMIT)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                AddEditActivity.this.setPreviewImage(bm, false);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // TODO: retry
                                makeWarnToast(AddEditActivity.this, "Failed to load existing photo");
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

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if (gpsAlert != null) {
            gpsAlert.dismiss();
        }
        if (locationAlert != null) {
            locationAlert.dismiss();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    private void attemptSaveMood(boolean bypassLocationNull) {
        saveBtn.setEnabled(false);
        // requires an emotion to be selected
        if (selectedEmotion == null) {
            makeInfoToast(AddEditActivity.this, "Select an emotion");
            saveBtn.setEnabled(true);
            return;
        }

        boolean hasPhoto = this.hasPhoto;
        SocialSituation socialSit = (SocialSituation) socialSitSpinner.getSelectedItem();

        String reasonText;
        if (reasonEditText.getText() == null) {
            reasonText = null;
        } else {
            // validates reasonText input (checks <= 3 words, 20 characters enforced by ui)
            // https://stackoverflow.com/a/5864174
            reasonText = reasonEditText.getText().toString();
            String trim = reasonText.trim();
            if (!trim.isEmpty() && trim.split("\\s+").length > 3) {
                makeInfoToast(AddEditActivity.this, "Reason must be less than 4 words");
                saveBtn.setEnabled(true);
                return;
            }
        }

        String id;
        Date date;
        Double lat = null;
        Double lon = null;
        boolean hasLocation = false;
        // reuses parameters if editing
        if (isAddActivity()) {
            id = UUID.randomUUID().toString();
            date = new Date();
            if (locationCheckBox.isChecked()) {
                if (currentLocation == null) {
                    if (!bypassLocationNull) {
                        // creates a dialog to confirm that you want to add the mood
                        // even though location was not recorded
                        createLocationVerifyAlert();
                        return;
                    }
                    lat = lon = null;
                    hasLocation = false;
                } else {
                    lat = currentLocation.getLatitude();
                    lon = currentLocation.getLongitude();
                    hasLocation = true;
                }
            }
            stopGettingLocation();
        } else {
            //TODO Display message location not provided instead of empty map (Maybe)
            id = currentMood.getId();
            date = currentMood.getDate();
            lat = currentMood.getLat();
            lon = currentMood.getLon();
            hasLocation = locationCheckBox.isChecked();
        }

        Mood mood = new Mood(id, date, selectedEmotion, reasonText, hasPhoto, hasLocation, socialSit, lat, lon);
        if (isAddActivity()) {
            mc.addMood(username, mood);
        } else {
            mc.updateMood(username, mood);
        }

        // only uploads if the photo hasn't changed for optimization purposes
        if (hasPhoto && changedPhoto) {
            mc.uploadPhoto(inputPhotoPath, id);
        }

        finish();
    }

    private void updateCurrentLocation(@NonNull Location location) {
        this.currentLocation = location;
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();

        LatLng latLng = new LatLng(lat, lng);
        if (currentMarker == null) {
            currentMarker = gMap.addMarker(new MarkerOptions().position(latLng));
        } else {
            currentMarker.setPosition(latLng);
        }
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    // TODO move to some controller

    private void startGettingLocation() {
        // displays your cached current location if it already exists
        if (currentLocation != null) {
            updateCurrentLocation(currentLocation);
        }

        try {
            // checks if the gps is enabled
            // https://stackoverflow.com/a/843716
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                createGPSAlert();
                locationCheckBox.setChecked(false);
                return;
            }

            // gets last known location to initialize
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(AddEditActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, "last location: " + location);
                            if (location != null) {
                                updateCurrentLocation(location);
                            }
                        }
                    });

            // interval of 5 to 10 seconds
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10 * 10000);
            locationRequest.setFastestInterval(5 * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        } catch (SecurityException e) {
            Log.w(TAG, Log.getStackTraceString(e));
            makeWarnToast(this, "Unexpected error: cannot access location");
            locationCheckBox.setChecked(false);
        }
    }

    private void stopGettingLocation() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void createGPSAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS (location) is disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        gpsAlert = builder.create();
        gpsAlert.show();
    }

    private void createLocationVerifyAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Location is enabled but has not been recorded. Do you still want to post?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        attemptSaveMood(true);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        saveBtn.setEnabled(true);
                    }
                });
        locationAlert = builder.create();
        locationAlert.show();
    }

    private void setupMapView(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(AddEditActivity.this);

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
        switch (requestCode) {

            case GALLERY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createImageIntent();
                } else {
                    makeInfoToast(this, "Cannot access photos");
                }
                break;

            case FINE_LOCATION_PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isAddActivity()) {
                        startGettingLocation();
                    }
                } else {
                    // unchecks location and shows a warning
                    makeInfoToast(this, "Cannot access location");
                    locationCheckBox.setChecked(false);
                }
                break;

            default:
                Log.w(TAG, "unknown permission");
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
            this.inputPhotoPath = mc.getPhotoPath(data, getContentResolver());

            // TODO: preview image in add/edit
            // TODO: async
            // TODO: scale for faster load time?
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
        selectedEmotion = ((EmotionWithNull) adapterView.getItemAtPosition(i - 1)).toEmotion();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        // TODO: right zoom level or the target button to do that
        // TODO possibly display in AddActivity
        if (!isAddActivity()) {
            Double lat = currentMood.getLat();
            Double lon = currentMood.getLon();

            if (lat != null && lon != null) {
                LatLng latLng = new LatLng(lat, lon);
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }

    }
    @Override
    public void callback(CallbackId callbackId) {
        callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        // TODO stub
    }
}
