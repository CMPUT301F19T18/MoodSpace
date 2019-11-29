package com.example.moodspace;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for the emotion spinner
 * https://stackoverflow.com/a/48703213
 * https://stackoverflow.com/a/41637506
 */
public class EmotionAdapter extends ArrayAdapter<EmotionWithNull> {
    private final String TAG = EmotionAdapter.class.getSimpleName();
    private final String INITIAL_TEXT = getContext().getString(R.string.ae_initial_emotion_text);
    private static final int RESOURCE = R.layout.emotion_spinner_row;

    private Context context;

    public EmotionAdapter(Context context, List<EmotionWithNull> emotionList) {
        super(context, RESOURCE, emotionList);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        // Distinguish "real" spinner items (that can be reused) from initial selection item
        View row;
        if (convertView == null || (convertView instanceof TextView)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(RESOURCE, parent, false);
        } else {
            row = convertView;
        }

        EmotionWithNull currentItem = getItem(position);
        TextView emojiField = row.findViewById(R.id.emotion_spinner_row);

        if (currentItem == null) {
            Log.w(TAG, "Current item is null at position " + position);
        } else {
            String parsedText;
            if (currentItem == EmotionWithNull.NULL) {
                emojiField.setText(INITIAL_TEXT);
                row.setBackgroundColor(INITIAL_COLOR);
            } else {
                Emotion emotion = currentItem.toEmotion();
                parsedText = emotion.getEmojiString() + "      " + emotion.getEmojiName();
                emojiField.setText(parsedText);
                row.setBackgroundColor(emotion.getColorCode());
            }
        }
        return row;
    }

}
