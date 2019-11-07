package com.example.moodspace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class FilterFragment extends DialogFragment {
    private static final String TAG = FilterFragment.class.getSimpleName();
    private OnFragmentInteractionListener listener;
    private String username;
    private List<Emotion> emotionList;
    FilterController fc;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean[] checkedItems;

    public FilterFragment() {

    }

    public FilterFragment(String user, boolean[] checkedItems) {
        this.username = user;
        this.checkedItems = checkedItems;
    }

    public interface OnFragmentInteractionListener {
        void onOkPressed(boolean[] checkedItems);
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
        String[] emotionStrings = Emotion.HAPPY.getEmojiList();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(emotionStrings, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
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
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onOkPressed(getCheckedItems());
                    }}).create();
    }

    public boolean[] getCheckedItems(){
        return this.checkedItems;
    }
}
