package com.example.moodspace;

import android.content.Context;
import android.graphics.Color;
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
 */
public class EmotionAdapter extends ArrayAdapter<Emotion> {
    private final String INITIAL_TEXT = getContext().getString(R.string.ae_initial_emotion_text);
    private final int INITIAL_COLOR = Color.parseColor("#7f8c8d");
    private static final int RESOURCE = R.layout.emotion_spinner_row;

    private Context context;
    private boolean initialTextWasShown;

    public EmotionAdapter(Context context, List<Emotion> emotionList, boolean initialTextWasShown) {
        super(context, RESOURCE, emotionList);
        this.context = context;
        this.initialTextWasShown = initialTextWasShown;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    public boolean selectionMade(Spinner spinner) {
        return !((TextView) spinner.getSelectedView()).getText().toString().equals(INITIAL_TEXT);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.emotion_spinner_row, parent, false
            );
        }

        // shows initial text and color if no selection was initially made
        if (!initialTextWasShown) {
            initialTextWasShown = true;
            LayoutInflater inflater = LayoutInflater.from(context);
            final TextView view = (TextView) inflater.inflate(RESOURCE, parent, false);
            view.setText(INITIAL_TEXT);
            view.setBackgroundColor(INITIAL_COLOR);
            return view;
        }

        TextView emojiField = convertView.findViewById(R.id.emotion_spinner_row);
        Emotion currentItem = getItem(position);

        if (currentItem != null) {
            String parsedText = currentItem.getEmojiString() + "      " + currentItem.getEmojiName();
            emojiField.setText(parsedText);
            convertView.setBackgroundColor(currentItem.getColorCode());
        }
        return convertView;

    }
}
