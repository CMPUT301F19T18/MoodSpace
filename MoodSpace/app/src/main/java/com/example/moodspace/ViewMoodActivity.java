package com.example.moodspace;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import io.paperdb.Paper;

public class ViewMoodActivity extends AppCompatActivity {

    private static final String TAG = ViewMoodActivity.class.getSimpleName();
    private String otherUsername;
    private String username;
    private String mood;
    private Mood currentMood = null;
    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mood);

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
        TextView dateInfo = findViewById(R.id.date);
        TextView timeInfo = findViewById(R.id.time);
        String parsedDate = Utils.formatDate(currentMood.getDate());
        String parsedTime = Utils.formatTime(currentMood.getDate());
        dateInfo.setText(parsedDate);
        timeInfo.setText(parsedTime);
        Emotion emotion = currentMood.getEmotion();
        moodInfo.setText(emotion.getEmojiString());

        //ConstraintLayout moodLayout = findViewById(R.id.moodLayout);
        String background = emotion.getEmojiName().toLowerCase();

        int id = getResources().getIdentifier(background,"drawable", getPackageName());
//        row.setBackgroundResource(id);
        moodInfo.setBackgroundResource(id);


    }
}
