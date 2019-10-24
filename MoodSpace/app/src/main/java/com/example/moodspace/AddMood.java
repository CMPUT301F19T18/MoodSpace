package com.example.moodspace;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

/**
 * This intent is used to add a new mood to the mood list, it takes in certain parameters and upon clicking the add mood, will create a new Mood object
 * and return it to the main activity, otherwise, it will catch any exceptions and notify the user.
 */
public class AddMood extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mood);

        Button dateButton = findViewById(R.id.dateButton);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new com.example.moodspace.DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        Button timeButton = findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new com.example.moodspace.TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        Button setMood = findViewById(R.id.okayBtn);

        //Upon clicking the okay button, there will be an intent to another activity to fill out the required information.
        setMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mood = findViewById(R.id.mood);
                EditText emotionalState = findViewById(R.id.emotionalState);
                EditText reason = findViewById(R.id.reason);

                Intent intent = new Intent();

                TextView dateView = findViewById(R.id.date);
                TextView timeView = findViewById(R.id.time);

                String date = dateView.getText().toString();
                String time = timeView.getText().toString();

                try {
                    intent.putExtra("newMood",
                            new Mood(
                                    mood.getText().toString(),
                                    DateUtils.parse(date + " " + time),
                                    emotionalState.getText().toString(),
                                    reason.getText().toString()
//                                    Double.parseDouble(distance.getText().toString()),
//                                    Double.parseDouble(speed.getText().toString()),
//                                    Integer.parseInt(cadence.getText().toString()),
                            )
                    );
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (ParseException e) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Invalid info",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    /**
     * Formats and sets the new time string.
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView startTime = findViewById(R.id.time);
        String newHour = String.format(Locale.US,"%02d", hourOfDay);
        String newMinute = String.format(Locale.US, "%02d", minute);
        startTime.setText(String.format("%s:%s", newHour, newMinute));
    }

    /**
     * Formats and sets the new date string.
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateUtils.formatDate(c.getTime());
        TextView textView = findViewById(R.id.date);
        textView.setText(currentDateString);
    }

}
