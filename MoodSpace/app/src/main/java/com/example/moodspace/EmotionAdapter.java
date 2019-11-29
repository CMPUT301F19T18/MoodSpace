package com.example.moodspace;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Used for the emotion spinner
 * https://stackoverflow.com/a/48703213
 * https://stackoverflow.com/a/41637506
 */
public class EmotionAdapter extends ArrayAdapter<EmotionWithNull> {
    private final String TAG = EmotionAdapter.class.getSimpleName();
    private final String INITIAL_TEXT = getContext().getString(R.string.ae_initial_emotion_text);
    private final int INITIAL_COLOR = Color.parseColor("#7f8c8d");
    private static final int RESOURCE = R.layout.emotion_spinner_row;

    private Context context;

    public EmotionAdapter(Context context, List<EmotionWithNull> emotionList) {
        super(context, RESOURCE, emotionList);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            return initialSelection(parent, true);
        }
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (position == 0) {
            return initialSelection(parent, false);
        }
        return getCustomView(position, convertView, parent);
    }


    @Override
    public int getCount() {
        // Adjust for initial selection item
        return super.getCount();
    }

    private View initialSelection(@NonNull ViewGroup parent, boolean dropdown) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(RESOURCE, parent, false);
        view.setText(INITIAL_TEXT);
        view.setBackgroundColor(INITIAL_COLOR);

        if (dropdown) { // Hidden when the dropdown is opened
            view.setHeight(0);
        }

        return view;
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        // Distinguish "real" spinner items (that can be reused) from initial selection item
        View row;
        if (convertView == null || (convertView instanceof TextView)) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.emotion_spinner_row, parent, false);
        } else {
            row = convertView;
        }

        position = position - 1; // Adjust for initial selection item
        EmotionWithNull currentItem = getItem(position);
        TextView emojiField = row.findViewById(R.id.emotion_spinner_row);

        if (currentItem == null) {
            Log.w(TAG, "Current item is null at position " + position);
        } else {
            Emotion emotion = currentItem.toEmotion();
            String parsedText = emotion.getEmojiString() + "      " + emotion.getEmojiName();
            emojiField.setText(parsedText);
            row.setBackgroundColor(emotion.getColorCode());
        }
        return row;
    }

}