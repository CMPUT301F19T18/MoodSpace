package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.paperdb.Paper;

import static com.example.moodspace.Utils.makeInfoToast;
import static com.example.moodspace.Utils.makeSuccessToast;
import static com.example.moodspace.Utils.makeWarnToast;

public class ProfileListActivity extends AppCompatActivity
        implements FilterFragment.OnFragmentInteractionListener,
        ControllerCallback, FollowController.OtherMoodsCallback {
    private static final String TAG = ProfileListActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CacheListener cacheListener = CacheListener.getInstance();

    private static final String MOOD_LIST_LISTENER_KEY = "moodspace.ProfileListActivity.moodListListenerKey";

    private MoodController mc;
    private FollowController fc;
    private FilterController ftc;

    ArrayAdapter<MoodView> moodAdapter;
    ArrayList<MoodView> moodDataList = new ArrayList<>();;
    boolean[] filterChecks = new boolean[Emotion.values().length];

    ArrayList<MoodView> cachedMoodList;
    final ArrayList<Emotion> cachedEmotionList = new ArrayList<>(Arrays.asList(Emotion.values()));

    private String moodId;
    private String username;
    private boolean feed = false;
    ListView moodList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_profile_list);
        mc = new MoodController(this);
        fc = new FollowController(this);
        ftc = new FilterController(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString(Utils.USERNAME_KEY);
            feed = extras.getBoolean("feed");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // sets up the menu button
        final DrawerLayout drawerLayout = findViewById(R.id.profile_layout);
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
                        makeInfoToast(ProfileListActivity.this, "Profile");
                        return true;
                    case R.id.nav_item_following:
                        Intent intent = new Intent(ProfileListActivity.this, FollowActivity.class);
                        intent.putExtra(Utils.USERNAME_KEY, username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.nav_item_feed:
                        Intent intent1 = new Intent(ProfileListActivity.this, ProfileListActivity.class);
                        intent1.putExtra(Utils.USERNAME_KEY, username);
                        intent1.putExtra("feed", true);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.nav_item_map:
                        Intent intent3 = new Intent(ProfileListActivity.this, MapsActivity.class);
                        intent3.putExtra(Utils.USERNAME_KEY, username);
                        startActivity(intent3);
                        finish();
                        return true;
                    case R.id.nav_item_log_out:
                        Paper.book().delete(UserController.PAPER_USERNAME_KEY);
                        Paper.book().delete(UserController.PAPER_PASSWORD_KEY);
                        Intent loginScreen = new Intent(ProfileListActivity.this, LoginActivity.class);
                        loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginScreen);
                        finish();
                        return true;

                    default:
                        return false;
                }
            }
        });

        moodList = findViewById(R.id.moodList);
        FloatingActionButton addBtn = findViewById(R.id.addMoodButton);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddMood(username);
            }
        });

        if (feed) {
            fc.getFollowingMoods(username);
        } else {
            moodAdapter = new MoodViewList(this, moodDataList);
            moodList.setAdapter(moodAdapter);

            updateData();

            // sets up EditMood on tapping any mood
            moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openEditMood(username, position);
                }
            });

            // for deleting moods
            // see onCreateContextMenu, onContextItemSelected
            registerForContextMenu(moodList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cacheListener.removeListener(MOOD_LIST_LISTENER_KEY);
    }

    /**
     * updates filters and moods from firestore
     * - note that if a refresh feature is ever implemented, this can be called
     */
    public void updateData() {
        // used so both have to be updated before the filters are applied
        final CompletedTaskCounter counter = new CompletedTaskCounter(2);

        // gets and caches user moods
        mc.getMoodList(username, MOOD_LIST_LISTENER_KEY, new MoodController.UserMoodsCallback() {
            @Override
            public void callbackMoodList(@NonNull String user, @NonNull List<Mood> userMoodList) {
                cachedMoodList = (ArrayList<MoodView>) MoodView.addUsernameToMoods(userMoodList, user);
                initializeComplete(counter);
            }
        });

        // gets and caches filters
        ftc.getFilters(username, new FilterController.GetFiltersCallback() {
            @Override
            public void callbackFilters(@NonNull String user, @NonNull HashSet<String> filters) {
                HashSet<Emotion> emotionFilters = new HashSet<>();
                for (String emotionString: filters) {
                    Emotion emotion = Emotion.valueOf(emotionString);
                    emotionFilters.add(emotion);
                }
                setChecksFromSet(emotionFilters);
                initializeComplete(counter);

            }
        });
    }

    /**
     * applies the filter to the mood list and properly displays it
     */
    public void initializeComplete(CompletedTaskCounter counter) {
        // only increments if not initialized
        if (!counter.isComplete()) {
            counter.incrementComplete();
        }

        if (!counter.isComplete()) {
            return;
        }

        // runs if complete, note that success isn't counted
        applyFilters();
    }

    /**
     * applies the filters to the cached mood list, and displays them
     */
    public void applyFilters() {
        moodDataList.clear();
        HashSet<Emotion> filteredOutEmotions = getFilteredOutEmotions();
        for (MoodView moodView: cachedMoodList) {
            if (!filteredOutEmotions.contains(moodView.getEmotion())) {
                moodDataList.add(moodView);
            }
        }
        moodAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        int index = info.position;
        moodId = moodDataList.get(index).getId();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                moodDataList.remove(info.position);
                db.collection("users")
                        .document(username)
                        .collection("Moods")
                        .document(moodId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Data deletion successful");
                                makeSuccessToast(ProfileListActivity.this, "Deleted mood");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Data deletion failed:");
                                Log.d(TAG, Log.getStackTraceString(e));
                                makeWarnToast(ProfileListActivity.this, "Error: did not delete mood");
                            }
                        });
                moodAdapter.notifyDataSetChanged();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Opens add Mood intent.
     */
    public void openAddMood(String username) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(Utils.USERNAME_KEY, username);
        startActivity(intent);
    }

    /**
     * Opens edit Mood intent.
     */
    public void openEditMood(String username, int position) {
        Mood mood = moodDataList.get(position);
        Intent intent = new Intent(getApplicationContext(), AddEditActivity.class);
        intent.putExtra("MOOD", mood);
        intent.putExtra(Utils.USERNAME_KEY, username);
        startActivity(intent);
    }

    /**
     * Creates the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!feed) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }
        return true;
    }

    /**
     * Defines on click behaviour for the toolbar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                new FilterFragment(username, filterChecks)
                        .show(getSupportFragmentManager(), "FILTER");
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * updates filters locally and in firestore
     */
    public void onOkPressed(boolean[] newFilterChecks) {
        ftc.updateFilters(username, filterChecks.clone(), newFilterChecks);

        // copies newFilterChecks -> filterChecks
        System.arraycopy(newFilterChecks ,0, filterChecks,0, newFilterChecks.length);

        applyFilters();
    }

    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        if (callbackId instanceof FilterCallbackId) {
            switch ((FilterCallbackId) callbackId) {
                case UPDATE_FILTER_FAIL:
                    makeWarnToast(this, "Error: filters could not be uploaded");
                    Log.w(TAG, "filters were not properly updated");
                    break;
                case UPDATE_FILTERS_COMPLETE:
                    Log.d(TAG, "filters updated successfully");
                    break;
                default:
                    Log.w(TAG, "unrecognized callback ID: " + callbackId);
            }

        } else if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case USER_READ_DATA_FAIL:
                case USER_TASK_NULL:
                case USER_NONEXISTENT:
                    makeWarnToast(this, "Error: unable to read user data");
                    Log.w(TAG, "Error: unable to read user data, callbackId=" + callbackId);
                    break;
                default:
                    Log.w(TAG, "unrecognized callback ID: " + callbackId);
            }
        } else {
            Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
        // TODO stub
    }

    @Override
    public void callbackFollowingMoods(@NonNull String user, @NonNull ArrayList<MoodView> followingMoodsList) {
        moodAdapter = new MoodViewList(this, followingMoodsList);
        moodList.setAdapter(moodAdapter);
    }

    // gets the boolean array for the filter fragment
    private void setChecksFromSet(HashSet<Emotion> filters) {
        Arrays.fill(filterChecks, true);

        for (Emotion emotion: filters) {
            int i = cachedEmotionList.indexOf(emotion);
            filterChecks[i] = false;
        }
    }

    // saves the current filters hashset given the checks from the filter fragment
    private HashSet<Emotion> getFilteredOutEmotions() {
        HashSet<Emotion> filters = new HashSet<>();
        final Emotion[] emotionArray = Emotion.values();

        for (int i = 0; i < filterChecks.length; i++){
            // only adds to the filters if it is unselected (false)
            if (!filterChecks[i]) {
                filters.add(emotionArray[i]);
            }
        }

        return filters;
    }

}