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

public class SocialSituationAdapter extends ArrayAdapter<SocialSituation> {
    public SocialSituationAdapter(Context context, List<SocialSituation> socialSitList) {
        super(context,0, socialSitList);
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
                    R.layout.social_sit_spinner_row, parent, false
            );
        }

        TextView field = convertView.findViewById(R.id.social_sit_spinner_row);
        SocialSituation currentItem = getItem(position);
        if (currentItem != null) {
            field.setText(currentItem.getDescription());
        }

        return convertView;
    }
}
