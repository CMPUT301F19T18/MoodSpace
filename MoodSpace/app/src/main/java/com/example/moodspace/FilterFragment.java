package com.example.moodspace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog box to select what to filter
 */
public class FilterFragment extends DialogFragment {
    private static final String TAG = FilterFragment.class.getSimpleName();

    private OnFragmentInteractionListener listener;
    private String username;
    private boolean[] checkedItems;

    public FilterFragment(String user, boolean[] initialChecks) {
        this.username = user;
        this.checkedItems = initialChecks.clone();
    }

    public interface OnFragmentInteractionListener {
        void onOkPressed(boolean[] checkedItems);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener ");
        }
    }

    /**
     * creates the dialog box to set filters
     *
     * @return simply whether each checkbox is checked or not
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] emotionStrings = Emotion.getEmojiList();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder.setTitle("Filter Moods")
                .setMultiChoiceItems(emotionStrings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i, boolean isChecked) {
                        checkedItems[i] = isChecked;
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onOkPressed(checkedItems);
                    }
                }).create();
    }
}
