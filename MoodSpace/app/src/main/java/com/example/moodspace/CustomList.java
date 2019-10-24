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
public class CustomList extends ArrayAdapter<Mood> {

    private ArrayList<Mood> listOfMoods;
    private Context context;

    public CustomList(Context context, ArrayList<Mood> listOfMoods ) {
        super(context,0,listOfMoods);
        this.listOfMoods = listOfMoods;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        }

        Mood mood = listOfMoods.get(position);

        TextView moodInfo = view.findViewById(R.id.mood);
        TextView date = view.findViewById(R.id.mood_date);
        TextView time = view.findViewById(R.id.mood_time);

        moodInfo.setText(String.valueOf(mood.getMood()));
        date.setText(DateUtils.formatDate(mood.getDateTime()));
        time.setText(DateUtils.formatTime(mood.getDateTime()));

        return view;
    }
}
