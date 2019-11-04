package com.example.moodspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This intent is used to add a new mood to the mood list, it takes in certain parameters and upon clicking the add mood, will create a new Mood object
 * and return it to the main activity, otherwise, it will catch any exceptions and notify the user.
 */
public class EditMood extends AppCompatActivity {
    private Toolbar toolbar;
    private List<Emotion> emotionList;
    private MoodAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mood);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Mood");
        setSupportActionBar(toolbar);

        initList();
        final Spinner spinnerEmotions = findViewById(R.id.emotionSelector);
        mAdapter = new MoodAdapter(this, emotionList);
        spinnerEmotions.setAdapter(mAdapter);
        final String username = getIntent().getStringExtra("USERNAME");

        Button saveMood = findViewById(R.id.saveBtn);
        Button backBtn = findViewById(R.id.backBtn);

        final Mood currentMood = (Mood) getIntent().getSerializableExtra("MOOD");
        int emotionIndex = mAdapter.getPosition(currentMood.getEmotion());
        spinnerEmotions.setSelection(emotionIndex);

        final AddEditController controller = new AddEditController(this);

        //Upon clicking the okay button, there will be an intent to another activity to fill out the required information.
        saveMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controller.updateMood(
                        username,
                        new Mood(
                                currentMood.getId(),
                                currentMood.getDate(),
                                (Emotion) spinnerEmotions.getSelectedItem()
                        )
                );
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initList() {
        emotionList = Arrays.asList(Emotion.values());
    }
}
