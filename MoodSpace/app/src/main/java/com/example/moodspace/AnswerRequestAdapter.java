package com.example.moodspace;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class AnswerRequestAdapter extends ArrayAdapter<String>{

    private List<String> users;
    private Context context;
    private FollowController fc;
    private String username;

    public AnswerRequestAdapter(Context context, List<String> users, String username, FollowController fc){
        super(context,0, users);
        this.users = users;
        this.username = username;
        this.context = context;
        this.fc = fc;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.answer_request, parent,false);
        }
        final String username = this.username;

        final String userReq = users.get(position);
        TextView userText = view.findViewById(R.id.request_user);
        userText.setText(userReq);

        ImageButton acceptBtn = view.findViewById(R.id.accept_button);
        ImageButton declineBtn = view.findViewById(R.id.decline_button);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc.acceptFollowRequest(username, userReq);
                users.remove(position);
                notifyDataSetChanged();
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fc.removeFollowRequest(userReq, username);
                users.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;

    }


}
