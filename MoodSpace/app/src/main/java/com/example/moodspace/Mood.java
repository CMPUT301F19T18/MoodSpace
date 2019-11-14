package com.example.moodspace;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Mood Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {

    private String id;
    private Emotion emotion;
    // both date and time are stored under the date variable (formatted in DateUtils)
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

