package com.example.moodspace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This intent is used to add a new mood to the mood list, it takes in certain parameters and upon clicking the add mood, will create a new Mood object
 * and return it to the main activity, otherwise, it will catch any exceptions and notify the user.
 */
public class AddMood extends AppCompatActivity {

    private List<Emotion> emotionList;
    private MoodAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);
        final String username = getIntent().getStringExtra("USERNAME");
        initList();
        final Spinner spinnerEmotions = findViewById(R.id.emotionSelector);

        Button setMood = findViewById(R.id.saveBtn);
        final AddEditController controller = new AddEditController();

        //Upon clicking the okay button, there will be an intent to another activity to fill out the required information.
        setMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.addMood(
                        username,
                        new Mood(
                                null,
                                new Date(),
                                (Emotion) spinnerEmotions.getSelectedItem()
                        )
                );
                finish();
            }
        });



        mAdapter = new MoodAdapter(this, emotionList);
        spinnerEmotions.setAdapter(mAdapter);
    }

    private void initList() {
        emotionList = Arrays.asList(Emotion.values());
    }
}
