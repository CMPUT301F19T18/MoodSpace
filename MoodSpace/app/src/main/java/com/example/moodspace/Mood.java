package com.example.moodspace;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the required parameters to create a Mood Object and their respective constructors to initialize.
 */
public class Mood implements Serializable {

    // Combined Date and time to datetime, which will be formatted with DateUtils to the respective forms when the app runs.
    private String id;
    private Emotion emotion;
    private Date date;
    private String reasonText;
    private Boolean hasPhoto;
    private String socialSit;

    public Mood(String id, Date date, Emotion emotion, String reasonText, boolean hasPhoto, String socialSit) {
        this.id = id;
        this.emotion = emotion;
        this.date = date;
        this.reasonText = reasonText;
        this.hasPhoto = hasPhoto;
        this.socialSit = socialSit;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSocialSit() {
        return socialSit;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public boolean getHasPhoto() {
        return this.hasPhoto;
    }

    public void setHasPhoto(boolean hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

}
