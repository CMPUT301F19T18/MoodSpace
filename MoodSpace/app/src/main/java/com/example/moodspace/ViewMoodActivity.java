package com.example.moodspace;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewMoodActivity extends AppCompatActivity {

    private String username;
    private String moodId;
    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mood);

        username = getIntent().getExtras().getString("username");


    }
}
