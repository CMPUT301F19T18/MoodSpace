package com.example.moodspace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterFragment extends DialogFragment {
    private static final String TAG = FilterFragment.class.getSimpleName();
    private OnFragmentInteractionListener listener;
    private String username;
    private List<Emotion> emotionList;
    FilterController fc;

    public FilterFragment() {

    }

    public FilterFragment(String user) {
        this.username = user;
    }

    public interface OnFragmentInteractionListener {
        void onOkPressed();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener ");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        fc = new FilterController(FilterFragment.this.getActivity());
        String[] emotionList = Emotion.HAPPY.getEmojiList();
        List<Emotion> emotions = Arrays.asList(Emotion.values());
        boolean[] checkedItems = new boolean[emotionList.length];
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = fc.getChecked(username, emotions.get(i));
        }
        Log.w(TAG, java.util.Arrays.toString(checkedItems));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(emotionList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, filter it out
                    fc.filterOut(which, username);
                } else {
                    // Else, remove the item from the filter
                    fc.filterIn(which, username);
                }
            }
        });
        return builder
                .setTitle("Filter Moods")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onOkPressed();
                    }}).create();
    }
}
