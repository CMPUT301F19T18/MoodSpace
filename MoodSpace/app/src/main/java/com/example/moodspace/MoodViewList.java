package com.example.moodspace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This class formats and provides the custom list display for each mood object.
 */
public class MoodViewList extends ArrayAdapter<Mood> {

    private ArrayList<Mood> listOfMoods;
    private Context context;

    public MoodViewList(Context context, ArrayList<Mood> listOfMoods ) {
        super(context,0,listOfMoods);
        this.listOfMoods = listOfMoods;
        this.context = context;
    }

    /**
     * Displays emoji face, date, time and color in the list
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        }

        Mood mood = listOfMoods.get(position);

        TextView moodInfo = view.findViewById(R.id.mood);
        TextView date = view.findViewById(R.id.mood_date);
        TextView time = view.findViewById(R.id.mood_time);

        Emotion emotion = mood.getEmotion();
        moodInfo.setText(emotion.getEmojiString());
        date.setText(DateUtils.formatDate(mood.getDate()));
        time.setText(DateUtils.formatTime(mood.getDate()));

        view.setBackgroundColor(emotion.getColor());

        return view;
    }
}
