package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class ViewMoodActivity extends AppCompatActivity {

    private static final String TAG = ViewMoodActivity.class.getSimpleName();
    private String username;
    private String mood;
    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mood);

        username = getIntent().getExtras().getString("username");
        mood = getIntent().getExtras().getString("mood");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        toolbar.setTitle(username);
//        Log.d(TAG, mood);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        ImageView moodInfo = findViewById(R.id.emotionImage);
        TextView date = findViewById(R.id.mood_date);
        TextView time = findViewById(R.id.mood_time);
//        Emotion emotion = mood.getEmotion();
//        moodInfo.setText(emotion.getEmojiString());
//        date.setText(Utils.formatDate(mood.getDate()));
//        time.setText(Utils.formatTime(mood.getDate()));
//        String background = emotion.getEmojiName().toLowerCase();
//        int id = context.getResources().getIdentifier(background,"drawable", context.getPackageName());
//        moodLayout.setBackgroundResource(id);



    }
}
