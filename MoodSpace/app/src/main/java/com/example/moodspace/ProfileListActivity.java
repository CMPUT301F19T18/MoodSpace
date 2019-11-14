package com.example.moodspace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import java.util.Date;
import java.util.List;

public class ProfileListActivity extends AppCompatActivity implements FilterFragment.OnFragmentInteractionListener {
    private static final String TAG = ProfileListActivity.class.getSimpleName();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    ViewController vc = new ViewController();
    ArrayAdapter<Mood> moodAdapter;
    ArrayList<Mood> moodDataList;
    final boolean[] checkedItems = new boolean[Emotion.values().length];

    private String moodId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        username = getIntent().getExtras().getString(UserController.USERNAME_KEY);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView moodList = findViewById(R.id.moodList);
        FloatingActionButton addBtn = findViewById(R.id.addMoodButton);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddMood(username);
            }
        });
        final List<Emotion> filterList = new ArrayList<Emotion>();

        moodDataList = new ArrayList<>();
        moodAdapter = new MoodViewList(this, moodDataList);

        // sets up EditMood on tapping any mood
        moodList.setAdapter(moodAdapter);
        moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openEditMood(username, position);
            }
        });

        // sets up the menu button
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
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
                        Toast.makeText(ProfileListActivity.this,
                                "Profile", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_following:
                        Toast.makeText(ProfileListActivity.this,
                                "Following", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_map:
                        Toast.makeText(ProfileListActivity.this,
                                "Map", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_log_out:
                        Toast.makeText(ProfileListActivity.this,
                                "Log out", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        return false;
                }
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
                Toast.makeText(this, "Deleted mood", Toast.LENGTH_LONG).show();
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
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Data deletion failed" + e.toString());
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
        Intent intent = new Intent(this, com.example.moodspace.AddEditActivity.class);
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

    /**
     * Creates the toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
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
    public void update(String username, final List<Emotion> filterList) {
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
                            Emotion emotion = Emotion.valueOf(doc.getString("emotion"));
                            Date ts = doc.getTimestamp("date").toDate();
                            String reason = doc.getString("reasonText");
                            Boolean hasPhoto = doc.getBoolean("hasPhoto");
                            SocialSituation socialSit;
                            // TODO get rid once database is wiped
                            try { // backwards compatibility
                                socialSit = SocialSituation.valueOf(doc.getString("socialSituation"));
                            } catch (Exception ex) {
                                Log.d(TAG, "set default social situation instead");
                                Log.d(TAG, Log.getStackTraceString(ex));
                                socialSit = SocialSituation.NOT_PROVIDED;
                            }
                            if (hasPhoto == null) { // backwards compatibility
                                hasPhoto = false;
                            }

                            String id = doc.getId();
                            Mood newMood = new Mood(id, ts, emotion, reason, hasPhoto, socialSit);
                            if (filterList.contains(emotion)){
                                moodDataList.add(newMood);
                            }
                        }
                        moodAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void onOkPressed(boolean[] checkedItems){
        final String username = getIntent().getExtras().getString("Username");
        final Emotion[] emotionArray = Emotion.values();
        List<Emotion> filterList = new ArrayList<Emotion>();
        for (int i = 0; i < checkedItems.length; i++){
            if (checkedItems[i] == false) {
                filterList.add(emotionArray[i]);
            }
        }
        update(username, filterList);
    }
}