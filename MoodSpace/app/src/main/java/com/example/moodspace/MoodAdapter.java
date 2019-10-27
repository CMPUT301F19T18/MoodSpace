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
        TextView nameField = convertView.findViewById(R.id.emotionName);

        Emotion currentItem = getItem(position);

        if (currentItem != null) {
            emojiField.setText(currentItem.getEmojiString());
            nameField.setText(currentItem.emojiName);
            convertView.setBackgroundColor(currentItem.getColor());
        }
        return convertView;
    }
}
