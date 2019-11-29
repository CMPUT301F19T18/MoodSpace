package com.example.moodspace;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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

import static com.example.moodspace.Utils.makeWarnToast;

public class ViewMoodActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String TAG = ViewMoodActivity.class.getSimpleName();
    private String otherUsername;
    private MoodView currentMood = null;
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;

    private static final String MAPVIEW_BUNDLE_KEY = "moodspace.AddEditActivity.mapViewBundleKey";
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_view_mood);

        setupMapView(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentMood = (MoodView) extras.getSerializable("MOOD");
            otherUsername = currentMood.getUsername();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(otherUsername + "'s Mood");

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
        String parsedText = emotion.getEmojiString() + "      " + emotion.getEmojiName();
        moodInfo.setText(parsedText);
        //ConstraintLayout moodLayout = findViewById(R.id.moodLayout);
        String background = emotion.getEmojiName().toLowerCase();
        int id = getResources().getIdentifier(background,"drawable", getPackageName());
        moodInfo.setBackgroundResource(id);

        SocialSituation socialSit = currentMood.getSocialSituation();
        socialsitInfo.setText(socialSit.getDescription());

        String reasonTxt = currentMood.getReasonText();
        if (reasonTxt.equals("")) {
            reasonInfo.setText("Not Provided");
        } else {
            reasonInfo.setText(reasonTxt);
        }

        View leftsquareView = findViewById(R.id.left_square_view);
        View rightsquareView = findViewById(R.id.right_square_view);
        ImageView image = findViewById(R.id.image_view);
        TextView placeholderMsg = findViewById(R.id.placeholder_msg);
        Button imageButton = findViewById(R.id.image_button);
        imageButton.setVisibility(View.GONE);

        if (currentMood.getHasPhoto()) {
            String path = "mood_photos/" + currentMood.getId() + ".png";
            StorageReference storageRef = fbStorage.getReference().child(path);

            storageRef.getBytes(MAX_DOWNLOAD_LIMIT).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    ImageView imageView = findViewById(R.id.image_view);
                    imageView.setImageBitmap(bm);

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
        }

        if (currentMood.getHasLocation()) {
            mapView.setVisibility(View.VISIBLE);
            placeholderMsg.setVisibility(View.GONE);
        } else {
            rightsquareView.setVisibility(View.GONE);
            placeholderMsg.setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Double lat = currentMood.getLat();
        Double lon = currentMood.getLon();

        if (lat != null && lon != null) {
            LatLng latLng = new LatLng(lat, lon);
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}
