package com.example.moodspace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ControllerCallback, FollowController.OtherMoodsCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final String MAPVIEW_BUNDLE_KEY = "moodspace.MapsActivity.mapViewBundleKey";
    private static final String MOOD_LIST_LISTENER_KEY = "moodspace.ProfileListActivity.moodListListenerKey";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CacheListener cacheListener = CacheListener.getInstance();

    private FollowController fc;
    private MoodController mc;

    // location variables
    private GoogleMap mMap;
    MapView mMapView;
    private FusedLocationProviderClient fusedLocationProviderClient;

    TabLayout myTabs;

    private String username;
    private ArrayList<MoodView> followingMoodsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        username = getIntent().getExtras().getString(Utils.USERNAME_KEY);
        fc = new FollowController(this);
        mc = new MoodController(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Map");
        setSupportActionBar(toolbar);
        setupNavBar(toolbar);
        myTabs = findViewById(R.id.tabs);
        myTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mMap.clear();
                int position = tab.getPosition();
                switch (position){
                    case 0:
                        displayOwnMoods();
                        break;
                    case 1:
                        displayFollowingMoods();
                        break;
                    default:
                        Log.w(TAG, "unknown tab");
                }
                
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mMap.clear();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setupMapView(savedInstanceState);
        displayOwnMoods();
        fc.getFollowingMoods(username);

        //TODO make tabs and using mode and onclicklisteners refresh map with new markers
        //TODO marker color should be mood specific and should have a popup of username and emoji
        //TODO maybe on click viewactivity will open with that mood
    }


    private void displayFollowingMoods() {
        boolean centerCamera = false;

        for (MoodView m: followingMoodsList) {
            Double lat = m.getLat();
            Double lon = m.getLon();
            String ts = m.getDate().toString();
            Emotion emotion = m.getEmotion();
            String followingUser = m.getUsername();

            if (lat != null && lon != null) {
                LatLng latLng = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .title(followingUser + emotion.getEmojiString())
                        .snippet(ts)
                        .icon(Utils.getColorForMap(emotion)));

                // centers camera at the latest mood
                if (!centerCamera) {
                    centerCamera = true;
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }

    private void displayOwnMoods() {
        mc.getMoodList(username, MOOD_LIST_LISTENER_KEY, new MoodController.UserMoodsCallback() {
            @Override
            public void callbackMoodList(@NonNull String user, @NonNull List<Mood> userMoodList) {
                boolean centerCamera = false;

                for (Mood mood: userMoodList) {
                    Double lat = mood.getLat();
                    Double lon = mood.getLon();
                    Emotion emotion = mood.getEmotion();
                    String ts = mood.getDate().toString();

                    if (lat != null && lon != null) {
                        LatLng latLng = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions().position(latLng)
                                .title(emotion.getEmojiString())
                                .snippet(ts)
                                .icon(Utils.getColorForMap(emotion)));

                        // centers camera at the latest mood
                        if (!centerCamera) {
                            centerCamera = true;
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        }

                    }
                }
            }
        });
    }


    private void setupNavBar(Toolbar toolbar) {
        final DrawerLayout drawerLayout = findViewById(R.id.map_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // sets up navigation viewer (side bar)
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final TextView headerTextView
                = navigationView.getHeaderView(0).findViewById(R.id.header_text_view);
        headerTextView.setText(username);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_item_profile:
                        Intent intent = new Intent(MapsActivity.this, ProfileListActivity.class);
                        intent.putExtra(Utils.USERNAME_KEY, username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.nav_item_feed:
                        Intent intent1 = new Intent(MapsActivity.this, ProfileListActivity.class);
                        intent1.putExtra(Utils.USERNAME_KEY, username);
                        intent1.putExtra("feed", true);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.nav_item_following:
                        Intent intent2 = new Intent(MapsActivity.this, FollowActivity.class);
                        intent2.putExtra(Utils.USERNAME_KEY, username);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        finish();
                        return true;
                    case R.id.nav_item_map:
                        Intent intent3 = new Intent(MapsActivity.this, MapsActivity.class);
                        intent3.putExtra(Utils.USERNAME_KEY, username);
                        startActivity(intent3);
                        return true;
                    case R.id.nav_item_log_out:
                        Paper.book().delete(UserController.PAPER_USERNAME_KEY);
                        Paper.book().delete(UserController.PAPER_PASSWORD_KEY);
                        Intent loginScreen = new Intent(MapsActivity.this, LoginActivity.class);
                        loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginScreen);
                        finish();
                        return true;

                    default:
                        return false;
                }
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private void setupMapView(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(MapsActivity.this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
    }

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
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

        cacheListener.removeListener(MOOD_LIST_LISTENER_KEY);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // if you want own current location in maps activity fsr
        mMap = googleMap;

    }

    // Call Back Methods

    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        // TODO stub
    }

    @Override
    public void callbackFollowingMoods(@NonNull String user, @NonNull ArrayList<MoodView> followingMoodsList) {
            this.followingMoodsList = followingMoodsList;
    }
}
