package com.example.moodspace;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Mood Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {
    private static final String TAG = Mood.class.getSimpleName();

    private String id;
    private Emotion emotion;
    // both date and time are stored under the date variable (formatted in Utils)
    private Date date;
    private String reasonText;
    private boolean hasPhoto;
    private SocialSituation socialSituation;

    // apparently you need this?
    // https://firebase.google.com/docs/firestore/manage-data/add-data
    public Mood() {
    }

    public Mood(String id, Date date, Emotion emotion, String reasonText, boolean hasPhoto,
                SocialSituation socialSituation) {
        this.id = id;
        this.emotion = emotion;
        this.date = date;
        this.reasonText = reasonText;
        this.hasPhoto = hasPhoto;
        this.socialSituation = socialSituation;
    }

    /**
     * Reads and returns a mood from firestore
     */
    public static Mood fromDocSnapshot(DocumentSnapshot doc) {
        Emotion emotion = Emotion.valueOf(doc.getString("emotion"));
        Date date;
        Timestamp ts = doc.getTimestamp("date");
        if (ts == null) {
            Log.w(TAG, "date could not be read, using today's date insteada");
            date = new Date();
        } else {
            date = ts.toDate();
        }
        String reason = doc.getString("reasonText");
        Boolean hasPhoto = doc.getBoolean("hasPhoto");
        SocialSituation socialSit;
        // TODO get rid once database is wiped
        try { // backwards compatibility
            socialSit = SocialSituation.valueOf(doc.getString("socialSituation"));
        } catch (Exception ex) {
            Log.d(TAG, "set default social situation instead");
            Log.d(TAG, Log.getStackTraceString(ex));
            socialSit = SocialSituation.NOT_PROVIDED;
        }
        if (hasPhoto == null) { // backwards compatibility
            hasPhoto = false;
        }

        String id = doc.getId();
        return new Mood(id, date, emotion, reason, hasPhoto, socialSit);

    }

    @Exclude
    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public String getReasonText() {
        return reasonText;
    }

    public boolean getHasPhoto() {
        return this.hasPhoto;
    }

    public SocialSituation getSocialSituation() {
        return socialSituation;
    }
}

