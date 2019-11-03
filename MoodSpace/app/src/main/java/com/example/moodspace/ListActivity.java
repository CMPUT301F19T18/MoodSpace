package com.example.moodspace;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class ListActivity extends AppCompatActivity {
    Toolbar toolbar;
    ListView moodList;
    ArrayAdapter<com.example.moodspace.Mood> moodAdapter;
    ArrayList<com.example.moodspace.Mood> moodDataList;
    private FloatingActionButton button;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        moodList = findViewById(R.id.moodList);
        button = findViewById(R.id.addMoodButton);
        final String username = getIntent().getExtras().getString("Username");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddMood(username);
            }
        });

        moodDataList = new ArrayList<>();
        moodAdapter = new CustomList(this, moodDataList);

        moodList.setAdapter(moodAdapter);
        moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openEditMood(username, position);
            }
        });

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
                            String id = doc.getId();
                            Mood newMood = new Mood(id, ts, emotion);
                            newMood.setId(doc.getId());
                            moodDataList.add(newMood);
                        }

                        moodAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Opens add Mood intent.
     */
    public void openAddMood(String username) {
        Intent intent = new Intent(this, com.example.moodspace.AddMood.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    /**
     * Opens edit Mood intent.
     */
    public void openEditMood(String username, int position) {
        Mood mood = moodDataList.get(position);
        Intent intent2 = new Intent(getApplicationContext(), EditMood.class);
        intent2.putExtra("MOOD", mood);
        intent2.putExtra("USERNAME", username);
        startActivity(intent2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
}