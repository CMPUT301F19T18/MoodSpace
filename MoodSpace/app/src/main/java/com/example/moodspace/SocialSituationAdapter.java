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
    private static final int RESOURCE = R.layout.social_sit_spinner_row;
    private Context context;

    public SocialSituationAdapter(Context context, List<SocialSituation> socialSitList) {
        super(context, RESOURCE, socialSitList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent, true);
    }

    private View initView(int position, View convertView, ViewGroup parent, boolean isDropDown) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    RESOURCE, parent, false);
        }

        TextView field = convertView.findViewById(R.id.social_sit_spinner_row);
        SocialSituation currentItem = getItem(position);
        if (currentItem != null) {
            field.setText(currentItem.getDescription());
            if (isDropDown) {
                convertView.setBackgroundResource(R.drawable.original);
            }
        }

        return convertView;
    }
}
