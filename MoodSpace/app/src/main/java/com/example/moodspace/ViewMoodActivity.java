package com.example.moodspace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import io.paperdb.Paper;

import static com.example.moodspace.Utils.makeWarnToast;

public class ViewMoodActivity extends AppCompatActivity {

    private static final String TAG = ViewMoodActivity.class.getSimpleName();
    private String otherUsername;
    private String username;
    private String mood;
    private Mood currentMood = null;
    private FirebaseStorage fbStorage = FirebaseStorage.getInstance();
    private static final long MAX_DOWNLOAD_LIMIT = 30 * 1024 * 1024;
    private boolean hasPhoto = false;
    private boolean changedPhoto = false;


    ArrayAdapter<MoodOther> moodAdapter;
    ArrayList<MoodOther> moodDataList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mood);

        otherUsername = getIntent().getExtras().getString("USERNAME");
        username = getIntent().getExtras().getString("username");
        mood = getIntent().getExtras().getString("mood");
        currentMood = (MoodOther) getIntent().getSerializableExtra("MOOD");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(otherUsername);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        TextView moodInfo = findViewById(R.id.emotionText);
        TextView socialsitInfo = findViewById(R.id.situationText);
        TextView reasonInfo = findViewById(R.id.reason_text);
        TextView dateInfo = findViewById(R.id.date);
        TextView timeInfo = findViewById(R.id.time);

        String parsedDate = Utils.formatDate(currentMood.getDate());
        String parsedTime = Utils.formatTime(currentMood.getDate());
        dateInfo.setText(parsedDate);
        timeInfo.setText(parsedTime);

        Emotion emotion = currentMood.getEmotion();
        moodInfo.setText(emotion.getEmojiString());
        String parsedText = "      " + emotion.getEmojiString() + "      " + emotion.getEmojiName();
        moodInfo.setText(parsedText);
        //ConstraintLayout moodLayout = findViewById(R.id.moodLayout);
        String background = emotion.getEmojiName().toLowerCase();
        int id = getResources().getIdentifier(background,"drawable", getPackageName());
        moodInfo.setBackgroundResource(id);

        SocialSituation socialSit = currentMood.getSocialSituation();
        socialsitInfo.setText(socialSit.getDescription());

        String reasonTxt = currentMood.getReasonText();
        reasonInfo.setText(reasonTxt);

        View leftsquareView = findViewById(R.id.left_square_view);
        ImageView image = findViewById(R.id.image_view);
        Button imageButton = findViewById(R.id.image_button);

        if (currentMood.getHasPhoto()) {
            String path = "mood_photos/" + currentMood.getId() + ".png";
            StorageReference storageRef = fbStorage.getReference().child(path);

            storageRef.getBytes(MAX_DOWNLOAD_LIMIT).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ViewMoodActivity.this.setPreviewImage(bm, false);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // TODO: retry
                    makeWarnToast(ViewMoodActivity.this, "Failed to load existing photo");
                }
            });
        } else {
            leftsquareView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
        }

        if (currentMood.getHasLocation()) {

        }




    }

    private void setPreviewImage(Bitmap bm, boolean changedPhoto) {
        ImageView imageView = findViewById(R.id.image_view);
        Button imageButton = findViewById(R.id.image_button);
        imageView.setImageBitmap(bm);
        imageButton.setVisibility(View.GONE);

        // if ever true, then changedPhoto is true
        this.changedPhoto |= changedPhoto;
        this.hasPhoto = true;
    }

}
