package com.example.moodspace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Used for the emotion spinner
 */
public class EmotionAdapter extends ArrayAdapter<Emotion> {

    public EmotionAdapter(Context context, List<Emotion> emotionList) {
        super(context,0, emotionList);
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

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.emotion_spinner_row,parent, false
            );
        }
        TextView emojiField = convertView.findViewById(R.id.emotion_spinner_row);
        Emotion currentItem = getItem(position);

        if (currentItem != null) {
            if (currentItem.equals(Emotion.NULL)) {
                emojiField.setText("Please select a mood");
                convertView.setBackgroundColor(currentItem.getColorCode());
            }
            else {
                String parsedText = currentItem.getEmojiString() + "      " +  currentItem.getEmojiName();
                emojiField.setText(parsedText);
                convertView.setBackgroundColor(currentItem.getColorCode());
            }
        }
        return convertView;
    }
}
