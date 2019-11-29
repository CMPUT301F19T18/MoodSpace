package com.example.moodspace;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.example.moodspace.Utils.makeWarnToast;

public class ViewMoodActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = ViewMoodActivity.class.getSimpleName();
    private String otherUsername;
    private String username;
    private String mood;
    private Mood currentMood = null;
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;
    private boolean hasPhoto = false;
    private boolean changedPhoto = false;

    private static final String MAPVIEW_BUNDLE_KEY = "moodspace.AddEditActivity.mapViewBundleKey";
    private GoogleMap gMap;
    private MapView mapView;

    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mood);

        setupMapView(savedInstanceState);

        otherUsername = getIntent().getExtras().getString("USERNAME");
        username = getIntent().getExtras().getString("username");
        mood = getIntent().getExtras().getString("mood");
        currentMood = (MoodOther) getIntent().getSerializableExtra("MOOD");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(otherUsername);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        TextView moodInfo = findViewById(R.id.emotionText);
        TextView socialsitInfo = findViewById(R.id.situationText);
        TextView reasonInfo = findViewById(R.id.reason_text);
        TextView dateInfo = findViewById(R.id.date);
        TextView timeInfo = findViewById(R.id.time);
        mapView = findViewById(R.id.map_view);

        String parsedDate = Utils.formatDate(currentMood.getDate());
        String parsedTime = Utils.formatTime(currentMood.getDate());
        dateInfo.setText(parsedDate);
        timeInfo.setText(parsedTime);

        Emotion emotion = currentMood.getEmotion();
        moodInfo.setText(emotion.getEmojiString());
        String parsedText = "      " + emotion.getEmojiString() + "      " + emotion.getEmojiName();
        moodInfo.setText(parsedText);
        //ConstraintLayout moodLayout = findViewById(R.id.moodLayout);
        String background = emotion.getEmojiName().toLowerCase();
        int id = getResources().getIdentifier(background,"drawable", getPackageName());
        moodInfo.setBackgroundResource(id);

        SocialSituation socialSit = currentMood.getSocialSituation();
        socialsitInfo.setText(socialSit.getDescription());

        String reasonTxt = currentMood.getReasonText();
        reasonInfo.setText(reasonTxt);

        View leftsquareView = findViewById(R.id.left_square_view);
        View rightsquareView = findViewById(R.id.right_square_view);
        ImageView image = findViewById(R.id.image_view);
        TextView placeholderMsg = findViewById(R.id.placeholder_msg);
        Button imageButton = findViewById(R.id.image_button);

        if (currentMood.getHasPhoto()) {
            String path = "mood_photos/" + currentMood.getId() + ".png";
            StorageReference storageRef = fbStorage.getReference().child(path);

            storageRef.getBytes(MAX_DOWNLOAD_LIMIT).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ViewMoodActivity.this.setPreviewImage(bm, false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // TODO: retry
                    makeWarnToast(ViewMoodActivity.this, "Failed to load existing photo");
                }
            });
        } else {
            leftsquareView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
        }

        if (currentMood.getHasLocation()) {
            mapView.setVisibility(View.VISIBLE);
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
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(ViewMoodActivity.this);

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


    private void setPreviewImage(Bitmap bm, boolean changedPhoto) {
        ImageView imageView = findViewById(R.id.image_view);
        Button imageButton = findViewById(R.id.image_button);
        imageView.setImageBitmap(bm);
        imageButton.setVisibility(View.GONE);

        // if ever true, then changedPhoto is true
        this.changedPhoto |= changedPhoto;
        this.hasPhoto = true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        Double lat = currentMood.getLat();
        Double lon = currentMood.getLon();

        if (lat != null && lon != null) {
            LatLng latLng = new LatLng(lat, lon);
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}
