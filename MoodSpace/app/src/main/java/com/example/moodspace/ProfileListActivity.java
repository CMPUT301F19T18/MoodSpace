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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
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

    private ViewController vc;
    private FollowController fc;
    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList = new ArrayList<>();
    final boolean[] checkedItems = new boolean[Emotion.values().length];

    private String moodId;
    private String username;
    private boolean feed;
    ListView moodList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_profile_list);
        vc = new ViewController(this);
        fc = new FollowController(this);

        username = getIntent().getExtras().getString("username");
        feed = getIntent().getExtras().getBoolean("feed");

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
                        intent.putExtra("username", username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.nav_item_feed:
                        Intent intent1 = new Intent(ProfileListActivity.this, ProfileListActivity.class);
                        intent1.putExtra("username", username);
                        intent1.putExtra("feed", true);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.nav_item_map:
                        Intent intent3 = new Intent(ProfileListActivity.this, MapsActivity.class);
                        intent3.putExtra("username", username);
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

        moodAdapter = new MoodViewList(this, moodDataList);

        if (feed) {
            fc.getFollowingMoods(username);

            // sets up EditMood on tapping any mood
            moodList.setAdapter(moodAdapter);
            moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openViewMood(username, position);
                    MoodOther mood = MoodOther.getUsername();
                }
            });

        } else  {
            final List<Emotion> filterList = new ArrayList<>();

            // sets up EditMood on tapping any mood
            moodList.setAdapter(moodAdapter);
            moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    openEditMood(username, position);
                }
            });

            // sets up filters
            final Emotion[] emotionArray = Emotion.values();
            Arrays.fill(checkedItems, true);
            final CollectionReference cRef = db.collection("users")
                    .document(username)
                    .collection("Filter");
            cRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (QueryDocumentSnapshot doc : task.getResult()){
                        Emotion emotion = Emotion.valueOf(doc.getString("emotion"));
                        for (int i = 0; i < emotionArray.length; i++){
                            if (emotionArray[i] == emotion){
                                checkedItems[i] = false;
                                filterList.add(emotion);
                            }
                        }
                    }
                    update(username, filterList);
                }
            });

            registerForContextMenu(moodList);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        int index = info.position;
        Log.d(TAG, moodDataList.get(index).getId());
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
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    /**
     * Opens edit Mood intent.
     */
    public void openEditMood(String username, int position) {
        Mood mood = moodDataList.get(position);
        Intent intent = new Intent(getApplicationContext(), AddEditActivity.class);
        intent.putExtra("MOOD", mood);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    public void openViewMood(String username, int position) {
        Mood mood = moodDataList.get(position);
        Intent intent = new Intent(getApplicationContext(), ViewMoodActivity.class);
        intent.putExtra("MOOD", mood);
        intent.putExtra("USERNAME", username);
        Log.d(TAG, username);
        startActivity(intent);
    }


    /**
     * Creates the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!(feed)){
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
                new FilterFragment(username, checkedItems)
                        .show(getSupportFragmentManager(), "FILTER");
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // updates data from firestore
    public void update(final String username, final List<Emotion> filterList) {
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
                        moodDataList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Mood mood = Mood.fromDocSnapshot(doc);
                            if (!(filterList.contains(mood.getEmotion()))){
                                moodDataList.add(MoodOther.fromMood(mood, username));
                            }
                        }
                        moodAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void onOkPressed(boolean[] checkedItems){
        final Emotion[] emotionArray = Emotion.values();
        List<Emotion> filterList = new ArrayList<>();
        for (int i = 0; i < checkedItems.length; i++){
            if (checkedItems[i] == false) {
                filterList.add(emotionArray[i]);
            }
        }
        update(username, filterList);
    }

    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        // TODO stub
    }

    @Override
    public void callbackFollowingMoods(@NonNull String user, @NonNull ArrayList<MoodOther> followingMoodsList) {
        moodDataList.clear();
        moodDataList.addAll(followingMoodsList);

        moodList.setAdapter(moodAdapter);
        moodAdapter.notifyDataSetChanged();
    }
}