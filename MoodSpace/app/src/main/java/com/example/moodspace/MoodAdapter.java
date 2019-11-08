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
 * Used for the mood spinner
 */
public class MoodAdapter extends ArrayAdapter<Emotion> {

    public MoodAdapter(Context context, List<Emotion> moodList) {
        super(context,0, moodList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.emotion_spinner_row,parent, false
            );
        }
        TextView emojiField = convertView.findViewById(R.id.emoji);
        Emotion currentItem = getItem(position);

        if (currentItem != null) {
            if (currentItem.equals(Emotion.NULL)) {
                emojiField.setText("Please select a mood");
                convertView.setBackgroundColor(currentItem.getColor());
            }
            else {
                String parsedText = currentItem.getEmojiString() + "      " +  currentItem.getEmojiName();
                emojiField.setText(parsedText);
                convertView.setBackgroundColor(currentItem.getColor());
            }
        }
        return convertView;
    }
}
