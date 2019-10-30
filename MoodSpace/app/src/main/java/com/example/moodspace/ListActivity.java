package com.example.moodspace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class ListActivity extends AppCompatActivity {
    ListView moodList;
    ArrayAdapter<com.example.moodspace.Mood> moodAdapter;
    ArrayList<com.example.moodspace.Mood> moodDataList;
    private FloatingActionButton button;
    AddEditController ae;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        moodList = findViewById(R.id.moodList);
        button = findViewById(R.id.addMoodButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddMood();
            }
        });
        String username = getIntent().getExtras().getString("Username");


        moodDataList = new ArrayList<>();
        moodAdapter = new CustomList(this, moodDataList);

        moodList.setAdapter(moodAdapter);
        moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openEditMood(position);
            }
        });
        ae = new AddEditController(ListActivity.this);
        db.collection("users")
                .document(username)
                .collection("Moods")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable QuerySnapshot queryDocumentSnapshots,
                            @Nullable FirebaseFirestoreException e
                    ) {
                        moodDataList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Emotion emotion = Emotion.valueOf(doc.getString("emotion"));
                            Date ts = doc.getTimestamp("time").toDate();
                            doc.getId();
                            Mood newMood = new Mood(ts, emotion);
                            moodDataList.add(newMood);
                        }

                        moodAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Opens add Mood intent.
     */
    public void openAddMood() {
        Intent intent = new Intent(this, com.example.moodspace.AddMood.class);
        startActivityForResult(intent, 1);
    }

    /**
     * Opens edit Mood intent.
     */
    public void openEditMood(int position) {
        Mood mood = moodDataList.get(position);
        Intent intent2 = new Intent(getApplicationContext(), EditMood.class);
        intent2.putExtra("MOOD", mood);
        intent2.putExtra("POSITION", position);
        startActivityForResult(intent2, 2);
    }
    /**
     * First request code is for adding moods, will take the returned string mood, cast it and add it to the mood list.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String username = getIntent().getExtras().getString("Username");
                Serializable strNewMood = data.getSerializableExtra("newMood");

                Mood newMood = (Mood) strNewMood;
                ae.addMood(username, newMood);
            }
        }

        //Result code will be RESULT_OK when user selects save Mood info, it will take the updated mood and replace the mood indexed from returned position.
        //Will also notify the adapter of the changes.
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Serializable strUpdatedMood = data.getSerializableExtra("updatedMood");
                Serializable strPosition = data.getSerializableExtra("position");
                moodDataList.set((int) strPosition, (Mood) strUpdatedMood);
                moodAdapter.notifyDataSetChanged();
            }
        }
    }
}